# -*- coding: utf-8 -*-

#나름의 헤더파일 import
from motor import *

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
        stopcnt = 0
        gocnt = 0
        while True:
            if q.empty() == False:
                command = q.get()
                print(command)
                if command.startswith('UP'):
                    goforward(80)
                elif command.startswith('DOWN'):
                    gobackward(80)
                elif command.startswith('LEFT'):
                    leftturn()
                elif command.startswith('RIGHT'):
                    rightturn()
                elif command.startswith('STOP'):
                    stop()
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

    proc1.start()

    mainfunc(q)
    proc1.join()
