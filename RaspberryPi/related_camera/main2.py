import cv2
import numpy as np
from motor import *

#color Settings
Color_Lower = (35,130,46)
Color_Upper = (113,255,255)

#Camera Frame Settings
Frame_Width = 320
Frame_Height = 240
camera = cv2.VideoCapture(0)
camera.set(cv2.CAP_PROP_FRAME_WIDTH, Frame_Width)
camera.set(cv2.CAP_PROP_FRAME_HEIGHT, Frame_Height)

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

               if center[0] > Frame_Width/2+55:
                  turnRight()

               elif center[0] < Frame_Width/2-55:
                 turnLeft()
               else:
                  forward_2() #Fast Run

            elif radius <45 and radius > 25:

               if center[0] > Frame_Width/2+55:
                  turnRight()

               elif center[0] < Frame_Width/2-55:
                  turnLeft()

               else:
                  forward_1() #Low Run

            elif radius > 65:
               Reverse()

            else:
               brake()

         except:
            pass

      else:
         stop() #sign
         cv2.imshow("Frame", frame)
         if cv2.waitKey(1) & 0XFF == ord('q'):
            break

finally:
   camera.release()
   cv2.destroyAllWindows()
