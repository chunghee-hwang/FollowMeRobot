import bluetooth
import os # system() 
# 스마트폰 연결을 기다리는 함수
def acceptClient(server_socket):
    client_socket,address = server_socket.accept()
    print("Accepted connection from ",address)
    return client_socket
# 들어온 rssi 값에 따라 가까운지 먼지 string 반환
def measureDistance(rssi):
    if rssi_threshold < rssi: return "Too close!"
    else: return "Too far!"

rssi_threshold = -58.0 # 가까운지 먼지에 대한 rssi 기준

# 블루투스 서버 소켓 생성
server_socket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
port = 1 #스마트폰 코드와 동일한 블루투스 포트여야함.
server_socket.bind(("",port))
server_socket.listen(1)
client_socket = acceptClient(server_socket)#스마트폰이 접속할 때까지 기다림 

#속도 저하를 막기 위해 페어링 동작 중지
os.system("sudo systemctl stop AutoPair.service")
while 1:
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
    print("Received: %s" % msg)
    if msg.startswith("th"): # rssi_threshold 값이 들어왔다면 ex)th-55
        rssi_threshold = float(msg[2:]) # ex)th를 버리고 -55만 가져옴
        msg = "rssi_threshold change to %.2f" % rssi_threshold;
        print(msg+"\n")
        data = bytes(msg+'\n', 'utf-8')
        client_socket.send(data);
        continue
    else:
        msg = measureDistance(float(msg)) # 스마트폰과의 거리가 먼지 가까운지
    data = bytes(msg+"\n", 'utf-8') # 가까운지 먼지 string을 byte[]로 변환
    client_socket.send(data) # 가까운지 먼지를 스마트폰에게 전송
    print("Send: %s" % msg+"\n\n")
server_socket.close() # 블루투스 서버 소켓 닫음
