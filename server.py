import cv2
import time
import socket

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

print("Waiting for connection...")
client_socket, addr = server_socket.accept()
print(f"Connection from {addr} has been established!")
data = client_socket.recv(1024)
print(f"Received data from client: {data.decode()}")


def DummyWait(collected):
    time.sleep(5)
    return True


while True:
    ret, frame = video.read()
    if not ret:
        print("frame count: ", frameCount)
        break
    frameCount += 1
    if frameCount % (waitTime * video.get(cv2.CAP_PROP_FPS)):
        collectedFrames.append(frame)
    if len(collectedFrames) == 100:
        print(f"Time Taken: {waitTime * 100} seconds", )
        result = DummyWait(collectedFrames)

        print(len(collectedFrames))
        collectedFrames = []


video.release()
client_socket.close()
cv2.destroyAllWindows()
