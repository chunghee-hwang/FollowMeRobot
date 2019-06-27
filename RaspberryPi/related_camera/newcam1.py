import cv2
import numpy as np
from motor import *
from multiprocessing import Process, Value, Queue

#color Settings
#Color_Lower = (35,130,46)
#Color_Upper = (113,255,255)


def camera_func(r):
    #Camera Frame Settings
    Frame_Width = 320
    Frame_Height = 240
    camera = cv2.VideoCapture(0)
    camera.set(cv2.CAP_PROP_FRAME_WIDTH, Frame_Width)
    camera.set(cv2.CAP_PROP_FRAME_HEIGHT, Frame_Height)
    rgb=r.get()
    x_r=int(rgb[0:3])
    x_g=int(rgb[3:6])
    x_b=int(rgb[6:9])
    Color_Lower = (int(x_r),int(x_g),int(x_b))
    up_r=Color_Lower[0]-Color_Lower[0]/3
    up_g=Color_Lower[1]-(Color_Lower[1]/5)*3
    up_b=Color_Lower[2]-(Color_Lower[2]/5)*3
    Color_Upper = (int(up_r),int(up_g),int(up_b))
    print(up_r,up_g,up_b)
    try:
       while True:
          (_, frame) = camera.read()
          
          frame = cv2.GaussianBlur(frame, (11,11),1)
          hsv = cv2.cvtColor(frame,cv2.COLOR_BGR2HSV)
          mask = cv2.inRange(hsv, Color_Lower, Color_Upper)

          #Do erode if needed
          #mask = cv2.erode(mask, None, iterations=2)
          #Do dilate if needed
          #mask = cv2.dilate(mask, None, iterations=2)

          #contours
          _,contours,_ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
          center = None

          if len(contours) > 0:

             #Find the max length of contours
             c = max(contours, key=cv2.contourArea)

             #Find x,y,radius
             ((x,y), radius) = cv2.minEnclosingCircle(c)

             #Find the moments
             M = cv2.moments(c)

             try:
                #mass center
                center = (int(M["m10"]/M["m00"]),int(M["m01"]/M["m00"]))

                #process every frame
                cv2.circle(frame, (int(x), int(y)), int(radius),(0,255,255),2)
                cv2.circle(frame, center, 5, (0,0,255), -1)

                #Forward, Stop, Turn rules
                #Size of the recognized object

                if radius < 25 and radius > 5:

                   if center[0] > Frame_Width/2+40:
                      turnRight()

                   elif center[0] < Frame_Width/2-50:
                     turnLeft()
                   else:
                      forward_2() #Fast Run

                elif radius <75 and radius > 25:

                   if center[0] > Frame_Width/2+40:
                      turnRight()

                   elif center[0] < Frame_Width/2-50:
                      turnLeft()

                   else:
                      forward_1() #Low Run

                elif radius > 115:
                   Reverse()

                else:
                   brake()

             except:
                pass

          else:
             stop() #sign
             #cv2.imshow("Frame", frame)
             if cv2.waitKey(1) & 0XFF == ord('q'):
                break

    finally:
       camera.release()
       cv2.destroyAllWindows()
