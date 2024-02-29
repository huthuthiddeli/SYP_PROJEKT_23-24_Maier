import cyberpi
import time
import mbuild

mbuild.quad_rgb_sensor.off_led(1)
time.sleep(1)
mbuild.quad_rgb_sensor.set_led("white", 1)

i =1
while True:
    #x = mbuild.quad_rgb_sensor.get_line_sta(index = 1)
    #x = mbuild.quad_rgb_sensor.get_color_sta("L1", 1)
    x = mbuild.ultrasonic2.get(1)
    cyberpi.console.println(str(i) + ": " + str(x))
    i += 1
    time.sleep(0.5)
    
#cyberpi.mbot2.led_on(255,0,0, "all", 1)

#cyberpi.mbot2.forward(50, 1)
#cyberpi.mbot2.turn_left(30, 1)
#cyberpi.mbot2.backward(50, 1)
