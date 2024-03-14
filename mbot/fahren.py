import cyberpi as cpi
import mbuild
import time
import usocket
import urequests
import ujson
import os
import _thread

class SensorData:
    def __init__(self, ultrasonic, light, sound, angles, shake, front_light_sensors):
        self.ultrasonic = ultrasonic
        self.light = light
        self.sound = sound
        self.angles = angles
        self.shake = shake
        self.front_light_sensors = front_light_sensors

    def get_json(self):
        dict = {}
        dict["ultrasonic"] = self.ultrasonic
        dict["light"] = self.light
        dict["sound"] = self.sound
        dict["angles"] = self.angles
        dict["shake"] = self.shake
        dict["front_light_sensors"] = self.front_light_sensors
        return ujson.dumbs(dict)
     
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
        ])
    
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
        broadcast_socket.sendall(b' '.join([req_msg, str(self_port).encode('utf-8')]))
        try:
            response_data, server_address = recv_socket.recvfrom(2048)
            cpi.console.println('Server responded')
            cpi.led.on(0, 255, 0)
            
            if response_data == ack_msg:
                break
        except OSError as e:
            if e.args[0] == 110:  # errno 110: ETIMEDOUT
                cpi.console.println('Searching server...')
                cpi.led
        time.sleep(5)
        cpi.led.on(0, 0, 0)
        
    # Close UDP sockets
    recv_socket.close()
    broadcast_socket.close()
    return server_address
    
    
def connect_to_server(server_address):
    # Create UDP socket and connect to server
    tcp_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
    tcp_socket.connect(server_address)
    cpi.console.println('Connected to Server')
    return tcp_socket


def post_data_coroutine(post_route):
    while not STOP_THREADS:
        json = SensorData.read_sensor_data().get_json()
        urequests.post(post_route, data = json)
        cpi.console.print("Data sent...")
        time.sleep(0.5)
        
        
def receiving_coroutine(tcp_socket):
    global ANTI_SUICE_ON
    
    while not STOP_THREADS:
        data = tcp_socket.recv(1024)
        
        if not data:
            cpi.console.println("Server disconnected")
            cpi.led.on(255, 0, 0)
            stop_coroutines()
            
        if data == "SUI_ON":
            ANTI_SUICE_ON = True
        elif data == "SUI_OFF":
            ANTI_SUICE_ON = False
        elif data == "LINE_ON":
            LINE_FOLLOW_ON = True
        elif data == "LINE_OFF":
            LINE_FOLLOW_ON = False
        elif not LINE_FOLLOW_ON:
            try:
                values_strip = data.strip("[]")
                values = [int(num) for num in numbers_str.split(",")]
                
                if len(values) == 4:
                    cpi.console.println("LED")
                elif len(values) == 2:
                    move(values[0], values[1])
                else:
                    cpi.console.println("Illegal values!")
            except:
                cpi.console.println("Illegal values!")


def move(left, right):    
    if LINE_FOLLOW_ON:
        cpi.mbot2.drive_power(50, -50) #forward 

    while True:
        if ANTI_SUICE_ON:
            if cpi.ultrasonic2.get(index=1) < 15:
                cpi.mbot2.EM_stop(port="all")
        elif LINE_FOLLOW_ON:
            sensors = SensorData.read_front_light_sensors()
            
            if sensors[0] < 50 and sensors[1] < 50: 
                cpi.mbot2.drive_power(20, -20) #straight ahead 
                cpi.led.on(255,0,0,id=2) 
                cpi.led.on(255,0,0,id=4) 
                #cpi.console.println('straight')
            elif sensors[0] < 50: 
                cpi.mbot2.drive_power(5, -20) #turn left 
                cpi.led.on(255,0,0,id=2) 
                cpi.console.println('left')
            elif sensors[1] < 50: 
                    cpi.mbot2.drive_power(20, -5) #turn right 
                    cpi.led.on(255,0,0,id=4) 
                    cpi.console.println('right')
            else: cpi.led.on(0,255,0)
        else:
            cpi.mbot2.drive_power(left, right)


def start_coroutines(tcp_socket, post_route):
    global STOP_THREADS
    STOP_THREADS = False
    
    _thread.start_new_thread(post_data_coroutine, (post_route,))
    _thread.start_new_thread(receiving_coroutine, (tcp_socket,))


def stop_coroutines():
    global STOP_THREADS
    STOP_THREADS = True


def START_MBOT():
    stop_coroutines()
    connect_wlan(WIFI_SSID, WIFI_PW)
    SERVER_IP = find_server(BROADCAST_IP, BROADCAST_PORT, SELF_PORT, REQ_MSG, ACK_MSG)
    cpi.console.println(str(SERVER_IP[0])+":"+str(SERVER_IP[1]))
    POST_ROUTE = WEB_PROTOCOL+str(SERVER_IP[0])+":"+str(WEB_SERVER_PORT)+API_ROUTE
    start_coroutines(connect_to_server(SERVER_IP), POST_ROUTE)
    

STOP_THREADS = False
ANTI_SUICE_ON = False
LINE_FOLLOW_ON = False

WIFI_SSID = "htljoh-public"
WIFI_PW = "joh12345"

BROADCAST_IP = "255.255.255.255"
BROADCAST_PORT = 5595
SELF_PORT = 6000
REQ_MSG = b"ACM"
ACK_MSG = b"ACM"

WEB_SERVER_PORT = 8080
WEB_PROTOCOL = "http://"
API_ROUTE = "/api/mbot/Data"

while True:
    if cpi.controller.is_press('a'):
        start_msg = ""
        if not STOP_THREADS:
            start_msg += "Starting..."
        else:
            start_msg += "Restarting..."
        cpi.console.println(start_msg)
        time.sleep(5)
        START_MBOT()