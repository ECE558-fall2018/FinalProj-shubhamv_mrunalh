from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2
import numpy as np
from threading import Thread
from robot import face_turn

class Video(Thread):
    def __init__(self, camera, cascPath):
        super(Video, self).__init__()
        self.camera = camera
        self.cascPath = cascPath

    def run(self):
        rawCapture = PiRGBArray(self.camera, size=(640, 480))
        faceCascade = cv2.CascadeClassifier(self.cascPath)

        # capture frames from the camera
        for cap in self.camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
            # grab the raw NumPy array representing the image, then initialize the timestamp
            # and occupied/unoccupied text
            frame = cap.array

            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

            faces = faceCascade.detectMultiScale(
                gray,
                scaleFactor=1.1,
                minNeighbors=5,
                minSize=(30, 30),
                flags=cv2.CASCADE_SCALE_IMAGE
            )

            # Draw a rectangle around the faces
            # for (x, y, w, h) in faces:
            #     face_turn(True)
            #     cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)

            if len(faces) > 0:
                (x, y, w, h) = faces[0]
                # print((x, y, w, h))
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                if (x < 40):
                    face_turn("left")
                elif (x+w > 600):
                    face_turn("right")

            # Display the resulting frame
            cv2.imshow('Video', frame)

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

            rawCapture.truncate()
            rawCapture.seek(0)

        cv2.destroyAllWindows();