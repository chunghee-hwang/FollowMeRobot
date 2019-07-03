import cv2
import numpy as np
from time import *
from motor import *
from blueserver import *
from multiprocessing import Process, Value, Queue

#color Settings
#Color_Lower = (35,130,46)
#Color_Upper = (113,255,255)

dir_flag = 1

def camera_func(r,g,b,gostop):
    #Camera Frame Settings
    Frame_Width = 320
    Frame_Height = 240
    #picam 키기 위한 함수 및 프레임 가로 세로 정해주기
    camera = cv2.VideoCapture(0)
    camera.set(cv2.CAP_PROP_FRAME_WIDTH, Frame_Width)
    camera.set(cv2.CAP_PROP_FRAME_HEIGHT, Frame_Height)
    #블루투스서버로 보내준 r로 rgb값 받음
    print('camera_func - r: ',r,'g:',g,'b:',b)
    #이게 upper인데 바꾸는게 더 귀찮아서 그냥 둠. 이게 upper

    Color = np.uint8([[[r,g,b]]])
    Color_mid = cv2.cvtColor(Color,cv2.COLOR_RGB2HSV)

    print(Color_mid)
    Up_h = Color_mid[0][0][0]+10
    Up_s = Color_mid[0][0][1]
    Up_v = Color_mid[0][0][2]
    Low_h = Color_mid[0][0][0]-10
    Low_s = Color_mid[0][0][1]/3
    Low_v = Color_mid[0][0][2]/3


    Color_Upper = (int(Up_h), int(Up_s), int(Up_v))
    Color_Lower = (int(Low_h), int(Low_s), int(Low_v))
    print(Color_Upper)
    print(Color_Lower)
    try:
      while True:
         sleep(0.1)
         while gostop.value == 1:
            (_, frame) = camera.read()
            #mask 씌우는 이유는 rgb 보내준 색상들 선택해서 
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
            #print("for contour")
            
            if len(contours) > 0:
               print("go")
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

                  if radius < 15 and radius > 5:

                     if center[0] > Frame_Width/2+40:
                        turnRight()
                        dir_flag = 1
                     elif center[0] < Frame_Width/2-50:
                       turnLeft()
                       dit_flag = -1
                     else:
                        forward_2() #Fast Run
                  elif radius <80 and radius > 15:
                     if center[0] > Frame_Width/2+40:
                        turnRight()
                        dir_flag = 1

                     elif center[0] < Frame_Width/2-50:
                        turnLeft()
                        dir_flag = -1
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
               if dir_flag == 1:
                   print("오른쪽으로 없어짐")
                   turnRight()
               elif dir_flag == -1:
                   print("왼쪽으로 없어짐")
                   turnLeft()
               #forward_1()
               #cv2.imshow("Frame", frame)
               if cv2.waitKey(1) & 0XFF == ord('q'):
                  break
    except:
      print("except")

    finally:
       camera.release()
       cv2.destroyAllWindows()

if __name__ == '__main__':
    r = Value('i', -999)
    g = Value('i', -999)
    b = Value('i', -999)

    gostop = Value('i', 0) #0 :stop, 1: go
    blueServProc = Process(target=start_server, args=(r,g,b,gostop))
    blueServProc.start()

    while r.value == -999 or g.value == -999 or b.value == -999:
        sleep(1)
        print('wait for rgb value')

    print('main - 받은 rgb값 : ', r.value, g.value, b.value)
    camera_func(r.value, g.value, b.value, gostop)

    blueServProc.join()
