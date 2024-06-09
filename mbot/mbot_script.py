import cyberpi as cpi
import mbuild
import time
import usocket
import urequests
import ujson
import os
import _thread
import sys

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
    
    
def connect_to_server(server_address):
    recv_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_STREAM)

    cpi.console.println("Server connection...")
    while True:
        try:
            recv_socket.connect(server_address)
            if recv_socket:
                cpi.console.println("Connected!")
                break
        except OSError as e:
            cpi.console.println("Server connection...")
        time.sleep(5)
    return recv_socket


def post_coroutine(post_route, post_header):
    global POST_THREAD_STOPPED

    while not STOP_THREADS:
        cpi.console.println("Data")
        json = SensorData.read_sensor_data().get_json()
        urequests.post(post_route, data = json.encode('UTF-8'), headers = post_header)
        time.sleep(0.5)
    POST_THREAD_STOPPED = True
        
        
def receiving_coroutine(tcp_socket):
    global RECEIVE_THREAD_STOPPED
    global STOP_THREADS 

    global ANTI_SUICE_ON
    ANTI_SUICE_ON = False
    
    global TRY_CONNECT
    TRY_CONNECT = False
    
    while not STOP_THREADS:
        suicide = False
        if ANTI_SUICE_ON:
            suicide = check_suicide()
            
        try:
            data = tcp_socket.recv(1024)
            data = data.decode()
            
            converted_data = data.split("!")
            data = converted_data[0]

            if data:
                if data == "STOP":
                    stop_motors()
                elif data == "ANTISUICIDE_ON":
                    ANTI_SUICE_ON = True
                elif data == "ANTISUICIDE_OFF":
                    ANTI_SUICE_ON = False
                elif data.split(":")[0] == "LED":
                    rgb_values = data.split(":")[1]
                    rgb_values = rgb_values.split(";")
                    set_leds(int(rgb_values[0]), int(rgb_values[1]), int(rgb_values[2]))
                else:
                    converted_values = joystick_to_motor_speed(float(data.split(';')[0]), float(data.split(';')[1]))
                    left = converted_values[0]
                    right = converted_values[1]
                    cpi.console.println(str(left)+":"+str(right))
                    
                    if ANTI_SUICE_ON:
                        if not suicide:
                            move(left, right)
                    else:
                        move(left, right)
            else:
                stop_motors()
                cpi.console.println("Server disconnected\n")
                time.sleep(2)
                TRY_CONNECT = True
                break
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
    tcp_socket.close()
    RECEIVE_THREAD_STOPPED = True
    

def joystick_to_motor_speed(y, x):
    div = int(round((100 * y) * x))
    left = 0
    right = 0

    if x > 0:
        if y < 0:
            right = int(round((100 * y) + abs(div)))
            left = int(round(100 * y))
        else:
            right = int(round((100 * y) - abs(div)))
            left = int(round(100 * y))
    else:
        if y < 0:
            left = int(round((100 * y) + abs(div)))
            right = int(round(100 * y))
        else:
            left = int(round((100 * y) - abs(div)))
            right = int(round(100 * y))
    return (left, right*(-1))
    
    
def move(left, right):
    stop_motors()
    cpi.mbot2.drive_power(left, right)
    
    
def check_suicide():
    if SensorData.read_sensor_data().ultrasonic < 15:
        cpi.audio.play("meow")
        stop_motors()
        cpi.mbot2.backward(speed = 30, run_time = 2)
        return True
    return False
    

def stop_motors():
    cpi.mbot2.EM_stop(port="all")
    

def set_leds(r,g,b):
    cpi.led.on(int(r),int(g),int(b))
    

def start_coroutines(tcp_socket, post_route, post_header):
    global STOP_THREADS
    STOP_THREADS = False
    
    _thread.start_new_thread(post_coroutine, (post_route, post_header,))
    _thread.start_new_thread(receiving_coroutine, (tcp_socket,))

    POST_THREAD_STOPPED = False
    RECEIVE_THREAD_STOPPED = False


def stop_coroutines():
    global STOP_THREADS
    global POST_THREAD_STOPPED
    global RECEIVE_THREAD_STOPPED

    STOP_THREADS = True
    
    while not POST_THREAD_STOPPED and not RECEIVE_THREAD_STOPPED:
        time.sleep(0.1)
        

def START_MBOT():
    if not cpi.network.is_connect:
        connect_wlan(WIFI_SSID, WIFI_PW)
    SERVER_IP = find_server(BROADCAST_IP, BROADCAST_PORT, SELF_PORT, REQ_MSG, ACK_MSG)
    cpi.console.println(str(SERVER_IP[0])+":"+str(SERVER_IP[1]))
    POST_ROUTE = WEB_PROTOCOL+str(SERVER_IP[0])+":"+str(WEB_SERVER_PORT)+API_ROUTE
    start_coroutines(connect_to_server((SERVER_IP[0],TCP_PORT)), POST_ROUTE, POST_HEADER)
    
    
def try_connect():
    global TRY_CONNECT
    
    while True:
        if TRY_CONNECT:
            stop_coroutines()
            START_MBOT()
            TRY_CONNECT = False
        time.sleep(5)

# ---------- CONFIG ---------- #
STOP_THREADS = False
POST_THREAD_STOPPED = True
RECEIVE_THREAD_STOPPED = True

TRY_CONNECT = True
ANTI_SUICE_ON = False
LINE_FOLLOW_ON = False

DRIVE_MAX_POWER = 300

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

# ---------- INITIAL START ---------- #
# Implement start event handler on btn press 'a' here:
cpi.console.println("Press 'a' to start")
while not cpi.controller.is_press('a'): 
    cpi.led.on(255,0,0)
_thread.start_new_thread(try_connect, ())