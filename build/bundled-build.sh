#!/bin/bash

staticpath="../chat-service/src/main/resources/static"

if [ -d $staticpath ]; then
    rm -r $staticpath 
fi

(
  cd "../frontend/";   
  ng build;
)

if [ ! -d $staticpath ] 
then
    mkdir $staticpath 
fi

cp -r ../frontend/dist/chatappfront/* ../chat-service/src/main/resources/static/

mvn clean package -f ../chat-service

if [ $? -eq 0 ]
then
  cp ../chat-service/target/chat-service-1.0.0.jar .
fi

if [ -d $staticpath ]; then
    rm -r $staticpath 
fi


