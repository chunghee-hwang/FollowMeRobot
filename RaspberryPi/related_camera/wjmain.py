# -*- coding: utf-8 -*-

#나름의 헤더파일 import
from motor import *
from hcsr04 import *

from time import sleep
from wj_blueserver import *
from multiprocessing import Process, Value, Queue
from newcam1 import *
def mainfunc(center, q):
    # GPIO 모드 설정 
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    try:
        while True:
            if q.empty() == False:
                command = q.get()
                print(command)
            #cls()
            #print ("CENTER = %.1f cm" % center.value)
            #if center.value >= 50 and far_close.value == 0:
            #if far_close.value == 0:
            #    setMotor(CH1, 45, FORWARD)
            #    setMotor(CH2, 40, FORWARD)
            #elif center.value < 50 and far_close.value == 1:
            #else:
            #    setMotor(CH1, 45, STOP)
            #    setMotor(CH2, 45, STOP) 
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
    #r = Queue()
    proc1 = Process(target=start_server, args=(q,))
    proc3 = Process(target=distance, args=(TRIGGER_CENTER, ECHO_CENTER, center))

    proc1.start()
    proc3.start()

    mainfunc(center, q)
    proc1.join()
    proc3.join()
