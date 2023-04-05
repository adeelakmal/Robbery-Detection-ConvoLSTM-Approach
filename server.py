import cv2
import time
import socket
import tensorflow as tf
import numpy as np

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

video = cv2.VideoCapture(0)
waitTime = 0.5
frameCount = 0
collectedFrames = []

# print("Waiting for connection...")
# client_socket, addr = server_socket.accept()
# print(f"Connection from {addr} has been established!")
# data = client_socket.recv(1024)
# print(f"Received data from client: {data.decode()}")


def getPrediction(collected):
    print(f"Input shape is: {np.asarray(collected).shape} ")
    a = []
    a.append(collected)
    print(np.asarray(a).shape)
    return new_model.predict(np.asarray(a))


while True:
    ret, frame = video.read()
    resized_frame = cv2.resize(frame, (64, 64))
    normalized_frame = resized_frame / 255
    if not ret:
        print("frame count: ", frameCount)
        break
    frameCount += 1
    if frameCount % (waitTime * video.get(cv2.CAP_PROP_FPS)):
        collectedFrames.append(normalized_frame)
    if len(collectedFrames) == 100:
        print(f"Time Taken: {waitTime * 100} seconds", )
        result = getPrediction(collectedFrames)

        print(np.argmax(result))
        print(CLASSES[np.argmax(result)])
        collectedFrames = []


video.release()
client_socket.close()
cv2.destroyAllWindows()
