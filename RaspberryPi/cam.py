import cv2
import numpy as np
from time import *
from motor import *
from blueserver import *
from multiprocessing import Process, Value, Queue

dir_flag = 1

def camera_func(r,g,b,r2,g2,b2,gostop,direction):
    global dir_flag
    #Camera Frame Settings
    Frame_Width = 320
    Frame_Height = 240
    #picam 키기 위한 함수 및 프레임 가로 세로 정해주기
    camera = cv2.VideoCapture(0)
    camera.set(cv2.CAP_PROP_FRAME_WIDTH, Frame_Width)
    camera.set(cv2.CAP_PROP_FRAME_HEIGHT, Frame_Height)
    #블루투스서버로 보내준 r로 rgb값 받음
    print('camera_func - r: ',r.value,'g:',g.value,'b:',b.value)
    #이게 upper인데 바꾸는게 더 귀찮아서 그냥 둠. 이게 upper

    TopColor = np.uint8([[[r.value,g.value,b.value]]])
    BottomColor = np.uint8([[[r2.value,g2.value,b2.value]]])
    
    TopColor_hsv = cv2.cvtColor(TopColor,cv2.COLOR_RGB2HSV)
    BottomColor_hsv = cv2.cvtColor(BottomColor,cv2.COLOR_RGB2HSV)

    print(TopColor_hsv)
    print(BottomColor_hsv)

    #top hsv
    TopUp_h = TopColor_hsv[0][0][0]+5
    TopUp_s = TopColor_hsv[0][0][1]
    TopUp_v = TopColor_hsv[0][0][2]
    TopLow_h = TopColor_hsv[0][0][0]-5
    TopLow_s = TopColor_hsv[0][0][1]/3
    TopLow_v = TopColor_hsv[0][0][2]/3

    #bottom hsv
    BottomUp_h = BottomColor_hsv[0][0][0]+5
    BottomUp_s = BottomColor_hsv[0][0][1]
    BottomUp_v = BottomColor_hsv[0][0][2]
    BottomLow_h = BottomColor_hsv[0][0][0]-5
    BottomLow_s = BottomColor_hsv[0][0][1]/3
    BottomLow_v = BottomColor_hsv[0][0][2]/3


    TopColor_Upper = (int(TopUp_h), int(TopUp_s), int(TopUp_v))
    TopColor_Lower = (int(TopLow_h), int(TopLow_s), int(TopLow_v))
    BottomColor_Upper = (int(BottomUp_h), int(BottomUp_s), int(BottomUp_v))
    BottomColor_Lower = (int(BottomLow_h), int(BottomLow_s), int(BottomLow_v))
    
    #print(Color_Upper)
    #print(Color_Lower)
    
    try:
      while True:         
         sleep(0.1)
         while gostop.value == 1:
            (_, frame) = camera.read()
            #mask 씌우는 이유는 rgb 보내준 색상들 선택해서 
            frame = cv2.GaussianBlur(frame, (11,11),1)
            hsv = cv2.cvtColor(frame,cv2.COLOR_BGR2HSV)
            mask1 = cv2.inRange(hsv, TopColor_Lower, TopColor_Upper)
            mask2 = cv2.inRange(hsv, BottomColor_Lower, BottomColor_Upper)
            
            #contours
            _,contours1,_ = cv2.findContours(mask1, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            _,contours2,_ = cv2.findContours(mask2, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            Topcenter = None
            Bottomcenter = None

            #print("for contour")
            
            if (len(contours1) > 0 ) and (len(contours2) > 0):
               #print("go")
               #Find the max length of contours
               c1 = max(contours1, key=cv2.contourArea)
               c2 = max(contours2, key=cv2.contourArea)

               #Find x,y,radius
               ((x,y), radius) = cv2.minEnclosingCircle(c1)
               ((x2,y2), radius2) = cv2.minEnclosingCircle(c2)

               #Find the moments
               M1 = cv2.moments(c1)
               M2 = cv2.moments(c2)
               
               try:
                  #mass center
                  Topcenter = (int(M1["m10"]/M1["m00"]),int(M1["m01"]/M1["m00"]))
                  Bottomcenter = (int(M2["m10"]/M2["m00"]),int(M2["m01"]/M2["m00"]))

                  #process every frame
                  cv2.circle(frame, (int(x), int(y)), int(radius),(0,255,255),2)
                  cv2.circle(frame, Topcenter, 5, (0,0,255), -1)

                  cv2.circle(frame, (int(x2), int(y2)), int(radius2),(0,255,255),2)
                  cv2.circle(frame, Bottomcenter, 5, (0,0,255), -1)
                  #cv2.imshow("Frame", frame)

                  #Forward, Stop, Turn rules
                  #Size of the recognized object
                  #print("TopCenter: ", Topcenter,", BotCenter", Bottomcenter);
                  if (radius < 15 and radius > 5):

                     if (Topcenter[0] > Frame_Width/2+55) and (Bottomcenter[0] > Frame_Width/2+55):
                         #if (direction.value==1 or direction.value==0):
                             turnRight()
                             
                             dir_flag = 1
                             
                     elif (Topcenter[0] < Frame_Width/2-55) and (Bottomcenter[0] > Frame_Width/2-55):
                         #if (direction.value==-1 or direction.value==0):
                             turnLeft()
                             dit_flag = -1
                             
                     elif (Frame_Width/2-55 < Topcenter[0] < Frame_Width/2+55 and Frame_Width/2-55 < Bottomcenter[0] < Frame_Width/2+55):
                     #elif (direction.value==0):
                     #else :
                     		#a=1

                     		forward_2() #Fast Run
                         
                  elif (radius <80 and radius > 15):
                    if (Topcenter[0] > Frame_Width/2+32) and (Bottomcenter[0] > Frame_Width/2+32):
                        #if (direction.value==1 or direction.value==0):
                            turnRight()
                            dir_flag = 1
                    elif (Topcenter[0] < Frame_Width/2-32) and (Bottomcenter[0] > Frame_Width/2-32):
                        #if  (direction.value==-1 or direction.value==0):
                            turnLeft()
                            dir_flag = -1
                    elif (Frame_Width/2-32 < Topcenter[0] < Frame_Width/2+32 and Frame_Width/2-32 < Bottomcenter[0] < Frame_Width/2+32):
                    #elif (direction.value==0):
                    #else:
                    	forward_1() #Low Run
                        #a = 2
                  elif radius > 115:
                      Reverse()

                  else:     
                      brake()

               except:
                   pass

            else:
               #print(dir_flag)
               stop() #sign
               if dir_flag == 1:
                #   print("오른쪽으로 없어짐")
                   turnRight()
               elif dir_flag == -1:
                 #  print("왼쪽으로 없어짐")
                   turnLeft()
               
               if cv2.waitKey(1) & 0XFF == ord('q'):
                  break
         brake()
    except Exception as e:
      print(e)

    finally:
       camera.release()
       cv2.destroyAllWindows()
       
if __name__ == '__main__':
    r = Value('i', -999)
    g = Value('i', -999)
    b = Value('i', -999)   
    r2 = Value('i', -999)
    g2 = Value('i', -999)
    b2 = Value('i', -999)
    gostop = Value('i', 0) #0 :stop, 1: go
    direction = Value('i',0) #0:straight, -1:left, 1:right
    blueServProc = Process(target=start_server, args=(r,g,b,r2,g2,b2,gostop,direction))
    blueServProc.start()

    while r.value == -999 or g.value == -999 or b.value == -999 or r2.value == -999 or g2.value==-999 or b2.value==-999:
        sleep(1)
        print('wait for rgb value')

    print('main - 받은 toprgb값 : ', r.value, g.value, b.value)

    print('main - 받은 botrgb값 : ', r2.value, g2.value, b2.value)
    camera_func(r, g, b,r2,g2,b2, gostop,direction)

    blueServProc.join()
