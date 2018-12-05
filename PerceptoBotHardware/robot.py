from gpiozero import Robot
import pyrebase
import os
import socket

s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.connect(('8.8.8.8', 1))  # connect() for UDP doesn't send packets
local_ip_address = s.getsockname()[0]

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
        robot.left(0.4)
    elif (message['data'] > 1):
        robot.right(0.4)
    else:
        robot.stop()


linear_stream = db.child("linearAcc").stream(linear_handler)
lateral_stream = db.child("lateralAcc").stream(lateral_handler)