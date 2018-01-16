import Net
import struct
import cv2
import numpy as np
import sys


def int_to_bytes(x):
    return x.to_bytes((x.bit_length() + 7) // 8, 'big')


def int_from_bytes(xbytes):
    return int.from_bytes(xbytes, 'big')


socket = Net.Socket("127.0.0.1")
socket.Connect()

while True:

    dataBytes = socket.Receive()
    props = struct.unpack(">III", dataBytes)

    width = props[0]
    height = props[1]
    size = props[2]

    data = socket.ReceiveAll(size)

    foundSize = len(data)

    img = np.fromstring(data, np.uint8, count=size)
    img = img.reshape(height, width, 3)

    cv2.imshow("Image", img)

    b = int_to_bytes(0xFFC0FFEE)
    socket.SendBytes(b)

    if cv2.waitKey() & 0xFF == ord('q'):
        break

cv2.destroyAllWindows()
