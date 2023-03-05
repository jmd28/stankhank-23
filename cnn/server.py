from flask import Flask, request, jsonify
import cv2
import torch
import numpy as np

model = torch.hub.load('ultralytics/yolov5', 'custom', path='new_model.pt')  # local model

app = Flask(__name__)

@app.route('/api/collisions', methods=['POST'])
def receive_data():
    data = request.get_json()
    
    byte_array = data['image']

    np_array = np.frombuffer(byte_array, np.uint8)
    img_decoded = cv2.imdecode(np_array, cv2.IMREAD_COLOR)
    results = model(img_decoded, size=900) # batch of images
    
    response = {'result': results.pandas().xyxy[0].to_dict(orient='records') }
    return jsonify(response)

if __name__ == '__main__':
    app.run()

# curl -X POST -H "Content-Type: application/json" -d '{"image":"aaa"}' http://localhost:5000/api/collisions