from flask import Flask, request, jsonify
import cv2
import torch
import numpy as np
from PIL import Image 
import io 

model = torch.hub.load('ultralytics/yolov5', 'custom', path='new_model.pt')  # local model

app = Flask(__name__)

@app.route('/api/collisions', methods=['POST'])
def receive_data():
    img = request.data
    image = Image.open(io.BytesIO(img))
    results = model(image, size=900) # batch of images
    
    response = {'result': results.pandas().xyxy[0].to_dict(orient='records') }
    return jsonify(response)

if __name__ == '__main__':
    app.run()