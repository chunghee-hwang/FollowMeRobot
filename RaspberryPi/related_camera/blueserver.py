import bluetooth
import os # system()
from multiprocessing import Process, Value, Queue
# 스마트폰 연결을 기다리는 함수
def acceptClient(server_socket):
    client_socket,address = server_socket.accept()
    print("Accepted connection from ",address)
    return client_socket

def start_server(r,g,b,gostop):
    print('start server!!')

    #페어링 동작 재시작
    #os.system("sudo systemctl restart AutoPair.service")

    #내장된 블루투스 안테나 스위치 끄기
    #os.system("sudo systemctl disable hciuart")
    
    #새로 연결한 블루수스 안테나 스위치 켜기
    #os.system("sudo rfkill unblock bluetooth")
    #os.system("sudo hciconfig hci0 up")

    # 블루투스 서버 소켓 생성
    server_socket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
    port = 1 #스마트폰 코드와 동일한 블루투스 포트여야함.
    server_socket.bind(("",port))
    server_socket.listen(1)
    client_socket = acceptClient(server_socket)#스마트폰이 접속할 때까지 기다림 
    #페어링 및 연결이 완료되면 속도 저하를 막기 위해 페어링 동작 중지
    #os.system("sudo systemctl stop AutoPair.service")
    #여기서 camera_func은 newcam1에서 만든 함수.
    while True:
        try:
            #스마트폰에서 메시지를 받음
            data = client_socket.recv(1024)
            #스마트폰쪽에서 연결을 끊었다면
        except bluetooth.btcommon.BluetoothError:
            print("Client has exited")
            client_socket.close() #클라이언트 소켓 닫음
            client_socket = acceptClient(server_socket) #새로운 스마트폰 연결 기다림
            continue
        msg = data.decode('utf-8') #메시지를 byte[]에서 string으로 변환
        #print("Received: %s" % msg)
        #msg가 int인지 확인한 후 에러가 뜨지 않으면 try가 계속 실행되어 r 프로세스에 msg가 넘어가서 rgb가 들어감.
        #int(msg)에서 에러가 뜨면 except ValueError로 들어가고 방향 전환 프로세스에 msg가 넘어감
        print(msg)
        try:
            int(msg)
            rgbStr = str(msg)
            r.value=int(rgbStr[0:3])
            g.value=int(rgbStr[3:6])
            b.value=int(rgbStr[6:9])
            print("RGB 받음")
        except ValueError:
            print("명령 받음")
            print('msg:', msg)
            if msg.startswith('GO'):
                gostop.value = 1
            elif msg.startswith('STOP'):
                gostop.value = 0
            else:
                print('gostop error!')
    server_socket.close() # 블루투스 서버 소켓 닫음
    os.system("sudo systemctl restart AutoPair.service")
