from socket import *

from time import ctime

HOST,PORT='',8088

BUFFER_SIZE=1024
ADDR=("localhost",PORT)
dest = gethostbyname("localhost")
print(dest)

tcpServerSocket = socket(AF_INET,SOCK_STREAM)

tcpServerSocket.bind(ADDR)

tcpServerSocket.listen(6)
print('wait for connection')

while True:

    tcpClientSocket,addr = tcpServerSocket.accept()
    print('connect to ：',addr)

    while True:
          #decode() bytes --> str
        data = tcpClientSocket.recv(BUFFER_SIZE).decode()
        if not data:
            break
        print('data=',data)
  
        tcpClientSocket.send(('[%s] %s'%(ctime(),data)).encode())

    tcpClientSocket.close()
tcpServerSocket.close()
