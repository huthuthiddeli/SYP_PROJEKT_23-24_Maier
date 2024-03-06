import cyberpi as cpi
import mbuild
import time
import usocket
import urequests
import json
import os
import uasyncio as asyncio

class SensorData:
    def __init__(self, ultrasonic, light, sound, angles):
        self.ultrasonic = ultrasonic
        self.light = light
        self.sound = sound
        self.angles = angles
    
    @staticmethod
    def read_sensor_data():
        return SensorData(cpi.cpi.ultrasonic2.get(index=1), 
        cpi.get_bri(), 
        cpi.get_loudness(), 
        [cpi.get_pitch(), cpi.get_yaw(), cpi.get_roll()]
        )
        

def connect_wlan(wifi_ssid, wifi_pw):
    cpi.network.config_sta(wifi_ssid, wifi_pw)
    cpi.console.println("Connect to " + wifi_ssid)
    time.sleep(2)
    
    # Connect to Wifi
    while True:
        b = cpi.network.is_connect()
        cpi.console.println("Connection: " + str(b))
        if b == False:
            cpi.led.on(255,0, 0)
            time.sleep(2)
        else:
            cpi.led.on(0,255, 0)
            time.sleep(1)
            break

    sockaddr = cpi.network.get_ip()
    cpi.console.println(sockaddr)
    
    gateway = cpi.network.get_gateway()
    cpi.console.println(gateway)
    

def find_server(broadcast_ip, broadcast_port, self_port, req_msg, ack_msg):
    # Create a UDP socket for sending broadcasts
    broadcast_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
    broadcast_socket.connect((broadcast_ip, broadcast_port))
    
    # Create a UDP socket for recv
    recv_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
    recv_socket.bind(('', self_port))
    recv_socket.settimeout(5)
    
    # Send broadcast and connect to server
    while True:
        broadcast_socket.sendall(b' '.join([req_msg, str(self_port).encode('utf-8')]))
        cpi.console.println('Broadcast sent')
        
        try:
            response_data, server_address = recv_socket.recvfrom(2048)
            cpi.console.println('Response received:' + response_data.decode('utf-8'))
            
            if response_data == ack_msg:
                break
        except OSError as e:
            if e.args[0] == 110:  # errno 110: ETIMEDOUT
                cpi.console.println('No response')
        time.sleep(5) 
        
    # Close UDP sockets
    recv_socket.close()
    broadcast_socket.close()
    return server_address
    
    
def connect_to_server(server_address):
    # Create UDP socket and connect to server
    tcp_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
    cpi.console.print(server_address)
    tcp_socket.connect(server_address)
    cpi.console.println('Connected to Server')
    return tcp_socket


async def post_data_coroutine(post_route):
    while True:
        urequests.post(post_route, data = json.dumbs(SensorData.read_sensor_data()))
        await asyncio.sleep(5)
        
        
async def receiving_coroutine(tcp_socket):
    while True:
        data = tcp_socket.recv(1024)
        cpi.console.println(data)


def start_coroutines(tcp_socket, post_route):
    cpi.console.print("Hello")
    time.sleep(5)
    # Create an event loop
    loop = asyncio.get_event_loop()
    
    # Start the coroutines
    loop.create_task(receiving_coroutine(tcp_socket))
    #loop.create_task(post_data_coroutine(post_route))
    
    # Run the event loop
    loop.run_forever()
            

WIFI_SSID = "htljoh-public"
WIFI_PW = "joh12345"

BROADCAST_IP = "255.255.255.255"
BROADCAST_PORT = 5595
SELF_PORT = 6000
REQ_MSG = b"ACM"
ACK_MSG = b"ACM"

connect_wlan(WIFI_SSID, WIFI_PW)
SERVER_IP = find_server(BROADCAST_IP, BROADCAST_PORT, SELF_PORT, REQ_MSG, ACK_MSG)
POST_ROUTE = SERVER_IP+"/api/mbot" #server ip is array aus ip und port -> geht nicht
start_coroutines(connect_to_server(SERVER_IP), POST_ROUTE)