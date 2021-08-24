#!/bin/bash

(
  cd ../frontend/;   
  ng build;
)

sudo cp -r ../frontend/dist/chatappfront /usr/share/nginx/html/
