import bluetooth
import os # system()
from newcam1 import * 
from multiprocessing import Process, Value, Queue
# 스마트폰 연결을 기다리는 함수
def acceptClient(server_socket):
    client_socket,address = server_socket.accept()
    print("Accepted connection from ",address)
    return client_socket

def start_server(q):
    print('start server!!')

    #페어링 동작 재시작
    os.system("sudo systemctl restart AutoPair.service")

    #내장된 블루투스 안테나 스위치 끄기
    os.system("sudo systemctl disable hciuart")
    
    #새로 연결한 블루수스 안테나 스위치 켜기
    os.system("sudo rfkill unblock bluetooth")
    os.system("sudo hciconfig hci0 up")

    # 블루투스 서버 소켓 생성
    server_socket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
    port = 1 #스마트폰 코드와 동일한 블루투스 포트여야함.
    server_socket.bind(("",port))
    server_socket.listen(1)
    client_socket = acceptClient(server_socket)#스마트폰이 접속할 때까지 기다림 
    #페어링 및 연결이 완료되면 속도 저하를 막기 위해 페어링 동작 중지
    os.system("sudo systemctl stop AutoPair.service")
    
    
    #rgb값 판단하는 거 보내주기
    r= Queue()
    proc2 = Process(target = camera_func, args=(r,))
    proc2.start()
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
        try:
            print(msg)
            int(msg)
            r.put(msg)
            print("That's int")
        except ValueError:
            print("That's not an int!")
            q.put(msg)
       # q.put(msg)
    server_socket.close() # 블루투스 서버 소켓 닫음
    os.system("sudo systemctl restart AutoPair.service")
    proc2.join()
