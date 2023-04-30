import cv2
import time
import socket

import tensorflow as tf
import numpy as np
from threading import Thread

# Import saved model
new_model = tf.keras.models.load_model('model1.h5')

# Possible classes
CLASSES = ["Robbery", "Burglary", "Normal", "Fighting"]
print(new_model.summary())


# To establush connection with client
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# host = socket.gethostname()
host = "127.0.0.1"
port = 8000
server_socket.bind((host, port))
server_socket.listen(1)
print(host)

# video = cv2.VideoCapture(0)
waitTime = 0.5
frameCount = 0
collectedFrames = []
arrangedFrames = [[]]*9

print("Waiting for connection...")
client_socket, addr = server_socket.accept()
# client_socket.setblocking(False)
print(f"Connection from {addr} has been established!")

size = 30000


def captureFrame():
    # receive the length of the serialized data
    videoNo = client_socket.recv(4)
    while len(arrangedFrames[0]) < 1000:

        data = bytearray()
        packet = client_socket.recv(size)
        data.extend(packet)

        # Check if received data is smaller than desired size
        if len(data) < size:
            padding_size = size - len(data)
            padding_value = b'\x00'

            # Copying received data
            padded_byte_array = bytearray(size)
            padded_byte_array[:len(data)] = data

            # Fill remaining space with padding value
            padded_byte_array[len(data):] = padding_value * padding_size

        else:
            # If received data is larger or equal to desired size, no padding is needed
            padded_byte_array = data
        np_array = np.frombuffer(padded_byte_array, dtype=np.uint8)
        # collectedFrames.append(np_array)
        arrangedFrames[videoNo].append(np_array)
        videoNo += 1


def arrangeFrames():
    # videoNo = 0
    while len(arrangedFrames[0]) < 1000:
        # if videoNo == 9:
        #     videoNo = 0
        # if len(collectedFrames) > 0:
        #     arrangedFrames[videoNo].append(collectedFrames.pop(0))
        #     videoNo += 1
        print(f"Received data from client: {len(arrangedFrames[0])}")
        # print(f"Received data from client: {arrangedFrames[0]}")


captureThread = Thread(target=captureFrame)
arrangeThread = Thread(target=arrangeFrames)

captureThread.start()
arrangeThread.start()

captureThread.join()
arrangeThread.join()
workedframes = []
out = cv2.VideoWriter('outpy.avi', cv2.VideoWriter_fourcc(
    'M', 'J', 'P', 'G'), 30, (100, 100))


print(arrangedFrames[0][0].shape)
print(len(arrangedFrames[0][0]))
for i in range(len(arrangedFrames[0])):
    workedframes.append(arrangedFrames[0].pop(0).reshape(100, 100, 3))
print(workedframes[100].shape)
for frame in workedframes:
    out.write(frame)


def getPrediction(collected):
    print(f"Input shape is: {np.asarray(collected).shape} ")
    a = []
    a.append(collected)
    print(np.asarray(a).shape)
    return new_model.predict(np.asarray(a))


# while True:
#     ret, frame = video.read()
#     resized_frame = cv2.resize(frame, (64, 64))
#     normalized_frame = resized_frame / 255
#     if not ret:
#         print("frame count: ", frameCount)
#         break
#     frameCount += 1
#     if frameCount % (waitTime * video.get(cv2.CAP_PROP_FPS)):
#         collectedFrames.append(normalized_frame)
#     if len(collectedFrames) == 100:
#         print(f"Time Taken: {waitTime * 100} seconds", )
#         result = getPrediction(collectedFrames)

#         print(np.argmax(result))
#         print(CLASSES[np.argmax(result)])
#         collectedFrames = []

client_socket.close()
cv2.destroyAllWindows()
