import cyberpi as cpi
import mbuild
import time
import usocket
import urequests
import ujson
import os
import _thread
import select

class SensorData:
    def __init__(self, ultrasonic, light, sound, angles, shake, front_light_sensors, IP):
        self.ultrasonic = ultrasonic
        self.light = light
        self.sound = sound
        self.angles = angles
        self.shake = shake
        self.front_light_sensors = front_light_sensors
        self.IP = IP

    def get_json(self):
        dict = {}
        dict["ultrasonic"] = self.ultrasonic
        dict["light"] = self.light
        dict["sound"] = self.sound
        dict["angles"] = self.angles
        dict["shake"] = self.shake
        dict["front_light_sensors"] = self.front_light_sensors
        dict["IP"] = self.IP
        return ujson.dumps(dict)
     
    @staticmethod
    def read_sensor_data():
        return SensorData(cpi.ultrasonic2.get(index=1), 
        cpi.get_bri(), 
        cpi.get_loudness(), 
        [cpi.get_pitch(), cpi.get_yaw(), cpi.get_roll()],
        cpi.get_shakeval(),
        [
            cpi.quad_rgb_sensor.get_gray('l2', index = 1),
            cpi.quad_rgb_sensor.get_gray('l1', index = 1),
            cpi.quad_rgb_sensor.get_gray('r1', index = 1),
            cpi.quad_rgb_sensor.get_gray('r2', index = 1)
        ],
        cpi.network.get_ip())
    
    @staticmethod
    def read_front_light_sensors():
        return [
            cpi.quad_rgb_sensor.get_gray('l2', index = 1),
            cpi.quad_rgb_sensor.get_gray('l1', index = 1),
            cpi.quad_rgb_sensor.get_gray('r1', index = 1),
            cpi.quad_rgb_sensor.get_gray('r2', index = 1)
        ]
        
        
def connect_wlan(wifi_ssid, wifi_pw):
    global TRY_CONNECT
    TRY_CONNECT = False
    
    cpi.network.config_sta(wifi_ssid, wifi_pw)
    cpi.console.println("Wlan connection...")
    # Connect to Wifi
    while True:
        b = cpi.network.is_connect()
        if b == False:
            cpi.led.on(255,0, 0)
            time.sleep(2)
        else:
            cpi.led.on(0,255, 0)
            time.sleep(1)
            break

    sockaddr = cpi.network.get_ip()
    cpi.console.println(sockaddr)
    
    time.sleep(5)
    cpi.led.on(0, 0, 0)
    

def find_server(broadcast_ip, broadcast_port, self_port, req_msg, ack_msg):
    global TRY_CONNECT
    TRY_CONNECT = False
    
    # Create a UDP socket for sending broadcasts
    broadcast_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
    broadcast_socket.connect((BROADCAST_IP, BROADCAST_PORT))
    
    # Create a UDP socket for recv
    recv_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
        
    recv_socket.bind(('', self_port))
    recv_socket.settimeout(5)
    
    # Send broadcast and connect to server
    cpi.console.println("Searching server...")
    cpi.led.on(255,0, 0)
    while True:
        try:
            broadcast_socket.sendall(b' '.join([req_msg, str(self_port).encode('utf-8')]))
            response_data, server_address = recv_socket.recvfrom(2048)
            cpi.console.println('Server responded')
            cpi.led.on(0, 255, 0)
            
            if response_data == ack_msg:
                break
        except OSError as e:
            if e.args[0] == 110:  # errno 110: ETIMEDOUT
                cpi.console.println('Searching server...')
                cpi.led.on(255,0,0)
            elif e.args[0] == 118:
                cpi.console.println('Wifi is not connected')
                cpi.led.on(255,0,0)
                connect_wlan(WIFI_SSID, WIFI_PW)
        time.sleep(5)
        cpi.led.on(0, 0, 0)
        
    # Close UDP sockets
    recv_socket.close()
    broadcast_socket.close()
    return server_address
    
    
def tcp_connect_server(server_socket):
    global TRY_CONNECT
    TRY_CONNECT = False
    
    recv_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_STREAM)

    cpi.console.println("Server connection...")
    while True:
        try:
            recv_socket.connect(server_socket)
            time.sleep(2)
            if recv_socket:
                cpi.console.println("Connected!")
                break
        except OSError as e:
            cpi.console.println("Server connection...")
    return recv_socket
    

# sending data via http post + receiving commands via tcp socket instead of 2 sockets -> no threading needed
def post_receive_data(server_socket, post_route, post_header):
    global ANTI_SUICE_ON
    ANTI_SUICE_ON = False
    
    global TRY_CONNECT
    TRY_CONNECT = False

    recv_socket = tcp_connect_server(server_socket)
    while True:
        if ANTI_SUICE_ON:
            check_suicide()
            
        try:
            json = SensorData.read_sensor_data().get_json()
            urequests.post(post_route, data = json.encode('UTF-8'), headers = post_header)

            readable, _, _ = select.select([recv_socket], [], [], 0.1)  # Warte maximal 1/10 Sekunde
            if readable:
                data = recv_socket.recv(1024)
                data = data.decode()
                cpi.console.println(data)
                
                if data:
                    if data == "ANTISUICIDE_ON":
                        ANTI_SUICE_ON = True
                    elif data == "ANTISUICIDE_OFF":
                        ANTI_SUICE_ON = False
                    elif data.split(":")[0] == "LED":
                        rgb_values = data.split(":")[1]
                        set_leds(int(rgb_values.split(";")[0]), int(rgb_values.split(";")[0]), int(rgb_values.split(";")[0]))
                    else:
                        left = int(data.split(';')[0])
                        right = int(data.split(';')[1])
                        
                        if ANTI_SUICE_ON:
                            if left < 0 and right < 0:
                                move(left, right)
                        else:
                            move(left, right)
        except OSError as e:
            if e.args[0] == 104 or e.args[0] == 32 or e.args[0] == 113:
                stop_motors()
                cpi.console.println("Server disconnected\n")
                time.sleep(2)
                TRY_CONNECT = True
                break
            else:
                cpi.console.println(e)
                break
        except ValueError:
            stop_motors()
            cpi.console.println("Invalid command\n")
        except IndexError:
            stop_motors()
            cpi.console.println("Invalid command\n")
        except Exception as e:
            cpi.console.println(e)
    recv_socket.close()
    #test if latency is distrubing
    
            
def move(left, right):
    stop_motors()
    cpi.mbot2.drive_power(left, right)
    
    
def check_suicide():
    if SensorData.read_sensor_data().ultrasonic < 15:
        stop_motors()
    

def stop_motors():
    cpi.mbot2.EM_stop(port="all")
    

def set_leds(r,g,b):
    cpi.led.on(r,g,b)
        

def START_MBOT():
    if not cpi.network.is_connect:
        connect_wlan(WIFI_SSID, WIFI_PW)
    SERVER_IP = find_server(BROADCAST_IP, BROADCAST_PORT, SELF_PORT, REQ_MSG, ACK_MSG)
    cpi.console.println(str(SERVER_IP[0])+":"+str(SERVER_IP[1]))
    POST_ROUTE = WEB_PROTOCOL+str(SERVER_IP[0])+":"+str(WEB_SERVER_PORT)+API_ROUTE
    post_receive_data((SERVER_IP[0], TCP_PORT), POST_ROUTE, POST_HEADER)

def try_connect():
    while True:
        if TRY_CONNECT:
            START_MBOT()
        time.sleep(5)

# ---------- CONFIG ---------- #
TRY_CONNECT = True
ANTI_SUICE_ON = False
LINE_FOLLOW_ON = False

WIFI_SSID = "htljoh-public"
WIFI_PW = "joh12345"

BROADCAST_IP = "255.255.255.255"
BROADCAST_PORT = 5595
TCP_PORT = 5000
SELF_PORT = 6000
REQ_MSG = b"ACM"
ACK_MSG = b"ACM"

WEB_SERVER_PORT = 8080
WEB_PROTOCOL = "http://"
POST_HEADER = {'Content-type': 'application/json'}
API_ROUTE = "/api/mbot/Data"

# Implement start event handler on btn press 'a' here:
_thread.start_new_thread(try_connect, ())


# TEST IF THREADING IS NEEDED!!!!!!!!!!!!!!!!