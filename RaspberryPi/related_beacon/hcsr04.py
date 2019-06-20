# 초음파센서로 거리 측정 함수

# 라즈베리파이 GPIO 패키지 
import RPi.GPIO as GPIO
import time
import os


# 실제 핀 정의
TRIGGER_CENTER = 24
ECHO_CENTER = 23


def distance(GPIO_TRIGGER, GPIO_ECHO, distance):
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    
    # 핀 설정 함수
    GPIO.setup(TRIGGER_CENTER, GPIO.OUT)
    GPIO.setup(ECHO_CENTER, GPIO.IN)
    while True:
        #GPIO.output(GPIO_TRIGGER, False)
        #time.sleep(0.5)
        
        # set Trigger to HIGH
        GPIO.output(GPIO_TRIGGER, True)
     
        # set Trigger after 0.01ms to LOW
        time.sleep(0.00001)
        GPIO.output(GPIO_TRIGGER, False)
     
        StartTime = time.time()
        StopTime = time.time()

        count = 0
        
        # save StartTime
        while GPIO.input(GPIO_ECHO) == 0:
            StartTime = time.time()
            count = count + 1
            if count > 10000:
                break
            #print("start")
     
        # save time of arrival
        while GPIO.input(GPIO_ECHO) == 1:
            StopTime = time.time()
            #print("stop")
     
        # time difference between start and arrival
        TimeElapsed = StopTime - StartTime
        # multiply with the sonic speed (34300 cm/s)
        # and divide by 2, because there and back
        distance.value = (TimeElapsed * 34300) / 2
        #print(distance.value)
        if distance.value < 0:
            distance.value = 0
        if distance.value >400:
            distance.value = 400

def cls():
    os.system('cls' if os.name == 'nt' else 'clear')
