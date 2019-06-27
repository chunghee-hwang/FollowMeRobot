# -*- coding: utf-8 -*-

# 라즈베리파이 GPIO 패키지 
import RPi.GPIO as GPIO
import os
import time
from time import sleep

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

# 모터 상태
STOP  = 0
FORWARD  = 1
BACKWARD = 2

# 모터 채널
CH1 = 0
CH2 = 1

# PIN 입출력 설정
OUTPUT = 1
INPUT = 0

# PIN 설정
HIGH = 1
LOW = 0

#PWM PIN
ENA = 12
ENB = 18

#GPIO PIN
IN1 = 19
IN2 = 13
IN3 = 6  
IN4 = 5   

def setPinConfig(EN, INA, INB):        
    GPIO.setup(EN, GPIO.OUT)
    GPIO.setup(INA, GPIO.OUT)
    GPIO.setup(INB, GPIO.OUT)
    # 100khz 로 PWM 동작 시킴 
    pwm = GPIO.PWM(EN, 100) 
    # 우선 PWM 멈춤.   
    pwm.start(0) 
    return pwm

#PWM PIN 설정
pwmA = setPinConfig(ENA, IN1, IN2)
pwmB = setPinConfig(ENB, IN3, IN4)


def goforward(speed):
    setMotor(CH1, speed, FORWARD)
    setMotor(CH2, speed, FORWARD)

def gobackward(speed):
    setMotor(CH1, speed, BACKWARD)
    setMotor(CH2, speed, BACKWARD)

def stop():
    setMotor(CH1, 45, STOP)
    setMotor(CH2, 45, STOP)

def leftturn():
    setMotor(CH1, 45, BACKWARD)
    setMotor(CH2, 45, FORWARD)

def rightturn():
    setMotor(CH1, 45, FORWARD)
    setMotor(CH2, 45, BACKWARD)

def leftturn2():
    setMotor(CH1, 20, FORWARD)
    setMotor(CH2, 45, FORWARD)

def rightturn2():
    setMotor(CH1, 45, FORWARD)
    setMotor(CH2, 20, FORWARD)

# 모터 제어 함수
def setMotorContorl(pwm, INA, INB, speed, stat):

    #모터 속도 제어 PWM
    pwm.ChangeDutyCycle(speed)
    
    if stat == FORWARD:
        GPIO.output(INA, HIGH)
        GPIO.output(INB, LOW)
        
    #뒤로
    elif stat == BACKWARD:
        GPIO.output(INA, LOW)
        GPIO.output(INB, HIGH)
        
    #정지
    elif stat == STOP:
        GPIO.output(INA, LOW)
        GPIO.output(INB, LOW)

        
# 모터 제어함수 간단하게 사용하기 위해 한번더 래핑(감쌈)
def setMotor(ch, speed, stat):
    if ch == CH1:
        #pwmA는 핀 설정 후 pwm 핸들을 리턴 받은 값이다.
        setMotorContorl(pwmA, IN1, IN2, speed, stat)
    else:
        #pwmB는 핀 설정 후 pwm 핸들을 리턴 받은 값이다.
        setMotorContorl(pwmB, IN3, IN4, speed, stat)
