
# -*- coding: utf-8 -*-

#나름의 헤더파일 import
from motor import *
from hcsr04 import *

# GPIO 모드 설정 
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

if __name__ == '__main__':
    try:
        while True:
            #모터 핀 설정
            #핀 설정후 PWM 핸들 얻어옴    

            center = distance(TRIGGER_CENTER, ECHO_CENTER)

            cls()
       
            print ("CENTER = %.1f cm" % center)                               
       
            if center >= 80:
                setMotor(CH1, 40, FORWARD)
                setMotor(CH2, 40, FORWARD)
                sleep(2)
            elif center < 80:
                setMotor(CH1, 40, STOP)
                setMotor(CH2, 40, STOP)
    

                                    
            time.sleep(0.05)
 
        # Reset by pressing CTRL + C
    except KeyboardInterrupt:
        print("Measurement stopped by User")
        GPIO.cleanup()
