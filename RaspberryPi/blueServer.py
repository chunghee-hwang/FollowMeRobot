import bluetooth
import os # system() 

count=0
proc=0
# 스마트폰 연결을 기다리는 함수
rssi_threshold = -58.0 # 가까운지 먼지에 대한 rssi 기준

def acceptClient(server_socket):
    client_socket,address = server_socket.accept()
    print("Accepted connection from ",address)
    return client_socket
# 들어온 rssi 값에 따라 가까운지 먼지 string 반환
def measureDistance(rssi):
    global rssi_threshold
    if rssi_threshold < rssi.value: return 1
    else: return 0

def start_server(far_close, rssi):
    far_close.value = -1
    rssi.value = -1
    global rssi_threshold
    print('start!!')

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

    while 1:
        try:
            #스마트폰에서 메시지를 받음
            data = client_socket.recv(10)
            #스마트폰쪽에서 연결을 끊었다면
        except bluetooth.btcommon.BluetoothError:
            print("Client has exited")
            far_close.value = -1
            rssi.value = -1
            client_socket.close() #클라이언트 소켓 닫음
            client_socket = acceptClient(server_socket) #새로운 스마트폰 연결 기다림

            continue
        msg = data.decode('utf-8') #메시지를 byte[]에서 string으로 변환
        #print("Received: %s" % msg)
        if msg.startswith("th"): # rssi_threshold 값이 들어왔다면 ex)th-55
            rssi_threshold = float(msg[2:]) # ex)th를 버리고 -55만 가져옴
            msg = "rssi_threshold change to %.2f" % rssi_threshold;
            #print(msg+"\n")
            #data = bytes(msg+'\n', 'utf-8')
            #client_socket.send(data);
            continue
        else:
            rssi.value = (float(msg))
            msg = measureDistance(rssi) # 스마트폰과의 거리가 먼지 가까운지
            far_close.value = msg
            print(msg)
        data = bytes(str(msg)+"\n", 'utf-8') # 가까운지 먼지 string을 byte[]로 변환
        client_socket.send(data) # 가까운지 먼지를 스마트폰에게 전송
        #print("Send: %s" % msg+"\n\n")
    server_socket.close() # 블루투스 서버 소켓 닫음
    os.system("sudo systemctl restart AutoPair.service")
