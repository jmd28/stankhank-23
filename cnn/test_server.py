import cv2
import torch
import numpy as np
import requests
import json
# from PIL import Image


CONFIDENCE_LIMIT = 0.7

COLLISION_SERVICE = 'http://localhost:5000/'

# Define the dimensions of the image
GAME_WIDTH = 1600
GAME_HEIGHT = 1000

# Define the box parameters
x_position = 200
y_position = 150
width = 100
height = 80

obj1 = {
    "x": x_position,
    "y": y_position,
    "w": width,
    "h": height
}

x_position = 230
y_position = 160
width = 100
height = 80

obj2 = {
    "x": x_position,
    "y": y_position,
    "w": width,
    "h": height
}



objects = {"abc": obj1, "cde": obj2}


# Create a blank image
img = np.ones((GAME_HEIGHT, GAME_WIDTH, 3), np.uint8) * 255

for obj in objects.values():
    # Define the box parameters
    x_position = obj["x"]
    y_position = obj["y"]
    width = obj["w"]
    height = obj["h"]

    # draw in cv2
    cv2.rectangle(img, (x_position, y_position), (x_position+width, y_position+height), (0, 0, 0), 2)

# encode into byte array
success, encoded_image = cv2.imencode('.jpg', img)
byte_array = encoded_image.tobytes()

headers = {"Content-Type": "application/octet-stream"}

response = requests.post(COLLISION_SERVICE + 'api/collisions', data=byte_array, headers=headers)
if response.status_code == 200:
    # [{'xmin': 197.58145141601562, 'ymin': 148.6814422607422, 'xmax': 322.0530090332031, 'ymax': 239.489013671875, 'confidence': 0.9486695528030396, 'class': 0, 'name': 'collision'}]
    data = response.json()
    data = data["result"]
    colliding_keys = []
    for d in data:
        if d["confidence"] < CONFIDENCE_LIMIT:
            continue
        curr_box_keys = []
        for key in objects.keys():
            obj = objects[key]
            if d["xmin"] <= obj["x"] and d["xmax"] >= obj["x"]+obj["w"] and d["ymin"] <= obj["y"] and d["ymax"] >= obj["y"] + obj["h"]:
                curr_box_keys.append(key)
        colliding_keys.append(curr_box_keys)

    # add colliding objects to q [[uuid, uuid, uuid]]
    if len(colliding_keys) > 0:
        print(colliding_keys)
