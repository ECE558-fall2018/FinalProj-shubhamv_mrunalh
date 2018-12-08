from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2
import numpy as np
from threading import Thread
from robot import face_turn

# This thread is initialized from the live streaming
# program. This thread runs in parallel with the live
# streaming broadcast.
class Video(Thread):
    def __init__(self, camera, cascPath):
        super(Video, self).__init__()
        self.camera = camera
        self.cascPath = cascPath

    def run(self):

        rawCapture = PiRGBArray(self.camera, size=(640, 480))
        faceCascade = cv2.CascadeClassifier(self.cascPath)

        # Get the raw capture from the camera frame-by-frame.
        for cap in self.camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):

            frame = cap.array

            # Convert the frame to grayscale.
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

            # Detect the face using the cascade classifier.
            # The faces array contains coordinates for all
            # of the faces detected in the frame. In this
            # case, we will just use the first face detected.
            faces = faceCascade.detectMultiScale(
                gray,
                scaleFactor=1.1,
                minNeighbors=5,
                minSize=(30, 30),
                flags=cv2.CASCADE_SCALE_IMAGE
            )

            if len(faces) > 0:
                (x, y, w, h) = faces[0]
                # print((x, y, w, h))

                # Display a bounding box around the
                # detected face.
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)

                # If the face is going out of the left
                # side of the robot, turn the robot to
                # the left and vice versa.
                # Since the size of the frame is 640x480
                # the edges are specified as 40 pixels on
                # either sides.
                if (x < 40):
                    face_turn("left")
                elif (x+w > 600):
                    face_turn("right")

            # Display the resulting frame
            cv2.imshow('Video', frame)

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

            # Empty the raw capture after every frame is
            # processed.
            rawCapture.truncate()
            rawCapture.seek(0)

        cv2.destroyAllWindows();