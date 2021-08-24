#!/bin/bash

(
  cd "../frontend/";   
  ng build;
)

if [ ! -d "../chat-service/src/main/resources/static" ] 
then
    mkdir "../chat-service/src/main/resources/static"
fi

cp -r ../frontend/dist/chatappfront/* ../chat-service/src/main/resources/static/


