import cyberpi as cpi
import time

cpi.reset_yaw()

while True:
    p = cpi.get_pitch()
    r = cpi.get_roll()#pitch angle
    y = cpi.get_yaw()

    s = "P/R/Y: " + str(p) + " " + str(r) + " " + str(y)
    cpi.console.println(s)
    time.sleep(0.5)
