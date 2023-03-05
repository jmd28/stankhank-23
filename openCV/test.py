import cv2
import numpy as np
import base64

# Define the dimensions of the image
img_width = 1000
img_height = 1600

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

# Encode the image as a jpeg
success, encoded_image = cv2.imencode('.jpg', img)
print(encoded_image.tobytes())

# Convert the encoded image to a base64 string
base64_string = base64.b64encode(encoded_image).decode('utf-8')

# Print the base64 string
# print(base64_string)
# # Display the image
# cv2.imshow("Image with box", img)


# cv2.waitKey(0)
# cv2.destroyAllWindows()