import cv2
import time
import socket
import struct
import json
import tensorflow as tf
import numpy as np
from threading import Thread

# Import saved model
new_model = tf.keras.models.load_model('new_model.h5')

# Possible classes
CLASSES = ["Robbery", "Normal"]
print(new_model.summary())


# To establush connection with client
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# host = socket.gethostname()
host = "127.0.0.1"
port = 5000
server_socket.bind((host, port))
server_socket.listen(1)
print(host)

# video = cv2.VideoCapture(0)
waitTime = 0.5
frameCount = 0
collectedFrames = []
arrangedFrames = {0: [],
                  1: [],
                  2: [],
                  3: [],
                  4: [],
                  5: [],
                  6: [],
                  7: [],
                  8: []}

print("Waiting for connection...")
client_socket, addr = server_socket.accept()
# client_socket.setblocking(False)
print(f"Connection from {addr} has been established!")

size = 30000


def captureFrame():
    count = 0
    while(1):
        while len(arrangedFrames[7]) < 100:

            # Receive the byte array size
            data = client_socket.recv(size+4)
            source_id = int.from_bytes(data[:4], 'big')
            frame_data = data[4:]

            print("Is this it? ", source_id)

            data = bytearray()
            data.extend(frame_data)

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

            if source_id not in arrangedFrames:
                arrangedFrames[source_id] = []

            # arrangedFrames[source_id].append(frame_data)

            arrangedFrames[source_id].append(cv2.resize(
                np_array.reshape(100, 100, 3), (64, 64)))
        file = open('myfile.txt', 'w')
        getResults(file)
        count += 1
        saveVideo(count)

        for key in arrangedFrames.keys():
            arrangedFrames[key] = []


def getResults(file):
    prediction = getPrediction(arrangedFrames.values())
    print(prediction)
    for i in prediction:
        if i[0] >= 0.60 and i[0] < 0.70:
            ans = "Warning"
        elif i[0] >= 0.70:
            ans = "Robbery"
        else:
            ans = "Normal"
        print(ans)
        file.write(ans + '\n')
    file.close()

#  The following function saves the 8th (webcam) video as outpy.avi


def saveVideo(count):
    workedframes = []
    out = cv2.VideoWriter('outpy'+str(count)+'.avi', cv2.VideoWriter_fourcc(
        'M', 'J', 'P', 'G'), 30, (64, 64))

    for i in range(len(arrangedFrames[7])):
        workedframes.append(arrangedFrames[7].pop(0))
    for frame in workedframes:
        out.write(frame)


def getPrediction(collected):
    a = []
    for video in collected:
        frames = getFrames(video)
        print(f"Input shape is: {np.asarray(frames).shape} ")
        a.append(np.asarray(frames))
    # print(np.asarray(a).shape)
    results = new_model.predict(np.asarray(a))
    return results


def getFrames(video):
    frames = []
    sequenceLength = 30
    skip = int(len(video)/sequenceLength)
    for i in range(sequenceLength):
        if i*skip <= len(video) and len(video) > 30:
            print("current: ", i*skip, "limit: ", len(video))
            frames.append(video[i*skip])
    return frames


captureThread = Thread(target=captureFrame)

captureThread.start()
captureThread.join()

# saveVideo()
client_socket.close()
cv2.destroyAllWindows()
