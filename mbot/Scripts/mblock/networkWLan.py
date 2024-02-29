import cyberpi
import time
import usocket
import ujson
import os

cyberpi.led.on(0, 0,255) #Lights up all the LEDs

#cyberpi.wifi.connect("FirmaMaFa", "FirmaMaFa10")
cyberpi.network.config_sta("FirmaMaFa", "FirmaMaFa10")

while True:
#    b = cyberpi.wifi.is_connect()
    b = cyberpi.network.is_connect()
    if b == False:
        cyberpi.led.on(255,0, 0)
        time.sleep(1)
    else:
        cyberpi.led.on(0,255, 0)
        break


sockaddr = cyberpi.network.get_ip()
cyberpi.console.println(sockaddr)

#subnet = cyberpi.network.get_subnet_mark()
#cyberpi.console.println(subnet)

gateway = cyberpi.network.get_gateway()
cyberpi.console.println(gateway)

s = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)

s.bind ((sockaddr, 1235))
i =1

name = "SUPERPI"
while True:
    s.sendto(name, ("192.168.10.255", 1234))
    i += 1
    time.sleep(1)
    cyberpi.led.on(255,255, 255)
    time.sleep(0.1)
    cyberpi.led.on(0,0, 0)
    b, adr = s.recvfrom(1024)
    txt = str(b, "utf-8")
    cyberpi.console.println(txt)
    cyberpi.console.println(str(adr))
    data = ujson.loads(txt)
    
    cyberpi.led.on(data['R'],data['G'], data['B'])
