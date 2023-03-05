import cv2
import torch
from PIL import Image

model = torch.hub.load('ultralytics/yolov5', 'custom', path='new_model.pt')  # local model

# Images
im1 = Image.open('/home/dc228/Documents/fun/stankhank-23/data/99.jpg')  # PIL image

results = model([im1], size=900) # batch of images

# Results
results.print()  
results.save()  # or .show()

results.xyxy[0]  # im1 predictions (tensor)
print(results.pandas().xyxy[0])  # im1 predictions (pandas)
