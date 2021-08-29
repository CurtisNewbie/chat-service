#!/bin/bash

staticpath="../chat-service/src/main/resources/static"

if [ -d $staticpath ]; then
    rm -r $staticpath 
fi

(
  cd ../frontend/;   
  ng build;
)

if [ ! -d $staticpath ] 
then
    mkdir $staticpath 
fi

cp -r ../frontend/dist/chatappfront/* ../chat-service/src/main/resources/static/

mvn clean package -f ../chat-service

jarpath="../chat-service/target/chat-service-1.0.0.jar";

if [ $? -eq 0 ]
then
    cp $jarpath .
    ssh -l zhuangyongj 192.168.10.128 "/home/zhuangyongj/exec/chatserver/kill.sh"
    scp $jarpath "zhuangyongj@192.168.10.128:~/exec/chatserver/chatserver.jar"
fi

if [ -d $staticpath ]; then
    rm -r $staticpath 
fi


