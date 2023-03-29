import cv2
video = cv2.VideoCapture(0)
waitTime = 0.5
frameCount = 0
collectedFrames = []
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
        print(len(collectedFrames))
        collectedFrames = []
# if cv2.waitKey(1) & 0xFF == ord('q'):
#     break

video.release()
cv2.destroyAllWindows()
