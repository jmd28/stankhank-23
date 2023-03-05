import cv2
import torch
import numpy as np
# from PIL import Image

model = torch.hub.load('ultralytics/yolov5', 'custom', path='new_model.pt')  # local model

# Define the dimensions of the image
img_width = 1600
img_height = 1000

# Create a black image
img = np.ones((img_height, img_width, 3), np.uint8) * 255

# Define the box parameters
x_position = 200
y_position = 150
width = 100
height = 80

# Draw a rectangle on the image
cv2.rectangle(img, (x_position, y_position), (x_position+width, y_position+height), (0, 0, 0), 2)


x_position = 230
y_position = 160
width = 100
height = 80

# Draw a rectangle on the image
cv2.rectangle(img, (x_position, y_position), (x_position+width, y_position+height), (0, 0, 0), 2)

# Images
# im1 = Image.open('/home/dc228/Documents/fun/stankhank-23/data/99.jpg')  # PIL image

# results = model([im1], size=900) # batch of images
results = model([img], size=900) # batch of images

# Results
results.print()  
results.save()  # or .show()

results.xyxy[0]  # im1 predictions (tensor)
print(results.pandas().xyxy[0])  # im1 predictions (pandas)
