from gpiozero import Robot
# Pyrebase is a Python binding for the Firebase API
import pyrebase
import time
import socket
# netifaces is used to obtain the wlan0 IP Address of the Pi
from netifaces import AF_INET
import netifaces as ni

local_ip_address = ni.ifaddresses('wlan0')[AF_INET][0]['addr']

# Robot is used to control the L289N motor driver
# Motor driver is connected to GPIO pins 7, 8, 9, 10
robot = Robot(left=(9,10), right=(7,8))

# Config file to connect to Firebase
config = {
    'apiKey': "AIzaSyD4_3GgD07jSsugc5Tht33Xsz_PisX4VuQ",
    'authDomain': "perceptobot.firebaseapp.com",
    'databaseURL': "https://perceptobot.firebaseio.com",
    'projectId': "perceptobot",
    'storageBucket': "perceptobot.appspot.com",
    'messagingSenderId': "766339239713"
}

firebase = pyrebase.initialize_app(config)
db = firebase.database()

# Set the local IP of the Pi on the firebase
# so that the app can read the value and start
# the live stream.
db.child("IP").set(local_ip_address)

# Move the robot forward when the accelerometer
# value changes on Firebase.

# The robot library also performs automatic
# PWM. We can use this to change the speed
# of the robot depending on how much the phone
# is tilted.
def linear_handler(message):
    if (db.child("direction").get().val()):
        robot.forward(message['data'])
    else:
        robot.backward(message['data'])

# Move the robot left or right when the accelerometer
# value changes on Firebase.
def lateral_handler(message):
    if (message['data'] < -1):
        robot.left(0.3)
    elif (message['data'] > 1):
        robot.right(0.3)
    else:
        robot.stop()

# Set up the listeners on the data. The callback function
# is triggered when the data changes on the Firebase DB.
# This is similar to the firebase event listeners in Java.
linear_stream = db.child("linearAcc").stream(linear_handler)
lateral_stream = db.child("lateralAcc").stream(lateral_handler)

# This function is used to turn the robot when the
# face gets close to the edge. Called from video.py
def face_turn(direction):
    if (db.child("faceDetection").get().val()):
        if direction == 'left':
            print(direction)
            robot.left(0.3)
        else:
            print(direction)
            robot.right(0.3)

        time.sleep(0.3)
        robot.stop()
