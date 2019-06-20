# -*- coding: utf-8 -*-

#나름의 헤더파일 import
from motor import *
from hcsr04 import *

from time import sleep
from blueServer import *
from multiprocessing import Process, Value, Queue

speed = 0.0
def mainfunc(q):
    # GPIO 모드 설정 
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    global speed
    try:
#        stopcnt = 0
#        gocnt = 0
        while True:
            if q.empty() == False:
                command = q.get()
                print(command)
                if command.startswith('STOP'):
#                    stopcnt = stopcnt + 1
#                    if stopcnt > 0:
                        stop()                    
#                        print('stop complete')
#                        gocnt = 0
#                elif command.startswith('GO'):
#                    gocnt = gocnt + 1
#                    if gocnt > 1:
#                        goforward(speed)
#                        print('go complete')
#                        stopcnt = 0
                elif command.startswith('LEFT'):
                    leftturn()
                    print('leftturn')
                elif command.startswith('RIGHT'):
                    rightturn()
                    print('rightturn')
#                elif command.startswith('SETSPEED'):
#                   speed = (float)(command.split()[1])
                   #goforward(speed)
#                   print('set speed complete')
                elif command.startswith('SETDIRECTION'):
                    direction = (int)(command.split()[1])
                    print('set direction', direction, 'complete')
            sleep(0.01)
        # Reset by pressing CTRL + C
    except KeyboardInterrupt:
        print("Measurement stopped by User")
        GPIO.cleanup()
        q.close()
        q.join_thread()

if __name__ == '__main__':

    center = Value('d', -1.0)
    q = Queue()
    proc1 = Process(target=start_server, args=(q,))
    
#    proc3 = Process(target=distance, args=(TRIGGER_CENTER, ECHO_CENTER, center))

    proc1.start()
#    proc3.start()

    mainfunc(q)
    proc1.join()
#    proc3.join()
