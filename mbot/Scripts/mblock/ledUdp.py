import cyberpi
import time
import usocket

cyberpi.led.on(0, 0,255) #Lights up all the LEDs

cyberpi.wifi.connect("FirmaMaFa", "FirmaMaFa10")

while True:
    b = cyberpi.wifi.is_connect()
    if b == False:
        cyberpi.led.on(255,0, 0)
        time.sleep(1)
    else:
        cyberpi.led.on(0,255, 0)
        break


s = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)

s.bind (("192.168.10.63", 1235))
i =1

while True:
    s.sendto(str(i), ("192.168.10.13", 1234))
    i += 1
    time.sleep(1)
    cyberpi.led.on(255,255, 255)
    time.sleep(0.1)
    cyberpi.led.on(0,0, 0)
    b, adr = s.recvfrom(1024)
    cmd = str(b, "utf-8")
    cyberpi.console.println(cmd)
    dd = cmd.split(":")
    cyberpi.console.println(str(adr))
    cyberpi.led.on(int(dd[0]),int(dd[1]), int(dd[2]))
