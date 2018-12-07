from gpiozero import Robot
import pyrebase
import time
import socket
from netifaces import AF_INET
import netifaces as ni

local_ip_address = ni.ifaddresses('wlan0')[AF_INET][0]['addr']

robot = Robot(left=(9,10), right=(7,8))

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

db.child("IP").set(local_ip_address)

def linear_handler(message):
    if (db.child("direction").get().val()):
        robot.forward(message['data'])
    else:
        robot.backward(message['data'])


def lateral_handler(message):
    if (message['data'] < -1):
        robot.left(0.3)
    elif (message['data'] > 1):
        robot.right(0.3)
    else:
        robot.stop()

linear_stream = db.child("linearAcc").stream(linear_handler)
lateral_stream = db.child("lateralAcc").stream(lateral_handler)


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
