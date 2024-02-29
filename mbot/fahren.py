import cyberpi as cpi
import mbuild
import time
import usocket
import json
import os
import uasyncio as asyncio

class SensorData:
    def __init__(self, light, distance):
        self.light = light
        self.distance = distance
    
def gatherSensorData():
    sensor_data = SensorData(0,0)
    return sensor_data

async def sendDataToServer(socket):
    while True:
        socket.send(json.dumps(gatherSensorData(), separators=None))
        await asyncio.sleep(5)
        
async def main_coroutine(tcp_socket):
    while True:
        data = tcp_socket.recv(1024)
        cpi.console.println(data)

        if data == b'2':
            cpi.mbot2.forward(speed=50)
            
 # Connect to WLAN
wifi_ssid = "htljoh-public"
wifi_pw = "joh12345"
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

# Create a UDP socket for sending broadcasts
broadcast_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
broadcast_socket.connect(("255.255.255.255", 5595))

# Create a UDP socket for recv
recv_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
recv_socket.bind(('', 5098))
recv_socket.settimeout(5)

# Send broadcast and connect to server
while True:
    broadcast_socket.sendall(b"ACM")
    cpi.console.println('Broadcast sent')
    
    try:
        response_data, server_address = recv_socket.recvfrom(1024)
        cpi.console.println('Response received:', response_data)
        
        if response_data == b'cock':
            break
    except OSError as e:
        if e.args[0] == 110:  # errno 110: ETIMEDOUT
            cpi.console.println('No response')
    time.sleep(5) 
    
# Close UDP sockets
recv_socket.close()
broadcast_socket.close()

# Create TCP socket and connect to server
tcp_socket = usocket.socket(usocket.AF_INET, usocket.SOCK_STREAM)
tcp_socket.connect(server_address)
cpi.console.println('Connected to Server')

# Create an event loop
loop = asyncio.get_event_loop()

# Start the coroutines
loop.create_task(main_coroutine(tcp_socket))
loop.create_task(sendDataToServer(tcp_socket))

# Run the event loop
loop.run_forever()