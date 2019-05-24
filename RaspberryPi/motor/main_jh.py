
# -*- coding: utf-8 -*-

# 라즈베리파이 GPIO 패키지 
from motor import *
from hcsr04 import *

# GPIO 모드 설정 
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

if __name__ == '__main__':
    try:
        while True:
            #핀 설정후 PWM 핸들 얻어옴
            
            speed = 20

            while True:
                if speed < 40:
                    cls()
                    setMotor(CH1, speed, FORWARD)
                    setMotor(CH2, speed, FORWARD)
                    speed = speed +1
                    print("SPEED = %.1f" % speed) 
                elif speed >=40 and speed <60:
                    cls()
                    setMotor(CH1, speed, FORWARD)
                    setMotor(CH2, speed, FORWARD)
                    speed = speed +5
                    print("SPEED = %.1f" % speed) 
                elif speed >=60 and speed <80:
                    cls()
                    setMotor(CH1, speed, FORWARD)
                    setMotor(CH2, speed, FORWARD)
                    speed = speed +10                
                    print("SPEED = %.1f" % speed)
                elif speed >80:
                    break

                sleep(0.5)
                speed = speed + 1
                
            setMotor(CH1, 40, STOP)
            setMotor(CH2, 40, STOP)   
                                        
            #time.sleep(0.05)
 
        # Reset by pressing CTRL + C
    except KeyboardInterrupt:
        print("Measurement stopped by User")
        GPIO.cleanup()
