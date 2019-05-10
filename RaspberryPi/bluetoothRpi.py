
import bluetooth

def acceptClient(server_socket):
    client_socket,address = server_socket.accept()
    print("Accepted connection from ",address)
    return client_socket

server_socket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
port = 1
server_socket.bind(("",port))
server_socket.listen(1)
client_socket = acceptClient(server_socket)
while 1:
    try:
        data = client_socket.recv(1024)
    except bluetooth.btcommon.BluetoothError:
        print("Client has exited")
        client_socket.close()
        client_socket = acceptClient(server_socket)
        continue	
    print("Received: %s" % data.decode('utf-8').rstrip())
    client_socket.send(data)
    print("Send: %s" % data.decode('utf-8').rstrip())
server_socket.close()
