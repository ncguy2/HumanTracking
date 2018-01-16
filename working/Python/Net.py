import socket


class ServerSocket(object):
    def __init__(self, host="0.0.0.0", port=12000):
        self.socket = socket.socket()
        self.host = host
        self.port = port

    def Bind(self):
        self.socket.bind((self.host, self.port))
        self.socket.listen(5)

    def Accept(self):
        return self.socket.accept()


class Socket(object):
    def __init__(self, host, port=12000, internal_socket=None):

        if internal_socket:
            self.socket = internal_socket
        else:
            self.socket = socket.socket()
        self.host = host
        self.port = port

    def Connect(self):
        self.socket.connect((self.host, self.port))

    def Send(self, payload):
        self.SendBytes(payload.encode())

    def SendBytes(self, data):
        self.socket.send(data)

    def Close(self):
        self.socket.close()

    def Receive(self, size=1024):
        return self.socket.recv(size)

    def ReceiveAll(self, size=1024):
        data = b''
        while len(data) < size:
            packet = self.Receive(size - len(data))
            if not packet:
                return data
            data += packet
        return data
