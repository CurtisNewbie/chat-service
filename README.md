# Chat-Service

Service for chatting, functionalities are mainly backed by Redis, messages are not persistent.

This app is ***not a standalone server***, it internally uses Dubbo for RPC to talk to other services. You must have `auth-service`  as well as other middlewares running to use it. To compile this app, you will also need to manually install the following modules & dependencies, these are all my repositories.

Two job beans are created that may run to cleanup some expired rooms:

- com.curtisnewbie.service.chat.job.ClearRoomJob
- com.curtisnewbie.service.chat.job.ClearExpiredPublicRoomListJob

These jobs are run by the `distributed-task-module`, you may schedule these two jobs by adding following two lines in the `task` table (for more information see the `distributed-task-module`):

|id |job_name      |target_bean |cron_expr    |app_group   |enabled|concurrent_enabled|
|---|--------------|------------|-------------|------------|-------|------------------|
|2  |clear expired room|clearRoomJob|0 0/1 * ? * *|chat-service|1      |0                 |
|3  |remove expired room from public room list|clearExpiredPublicRoomListJob|0 0/1 * ? * *|chat-service|1|0|                 

## Related-Services

- auth-service 
    - description: service for managing users, access log and operation log.
    - url: https://github.com/CurtisNewbie/auth-service

## Middleware

- MySQL
- Nacos (or others, e.g., Zookeeper, Redis, etc)
- RabbitMQ
- Redis

## Modules and Dependencies

This project depends on the following modules that you must manually install (using `mvn clean install`).

- curtisnewbie-bom
    - description: BOM file for dependency management
    - url: https://github.com/CurtisNewbie/curtisnewbie-bom
    - branch: main
    - under `/microservice` folder

- auth-module
    - description: for user authentication, security and integration with auth-service
    - url: https://github.com/CurtisNewbie/auth-module
    - branch: main 

- common-module
    - description: for common utility classes 
    - url: https://github.com/CurtisNewbie/common-module
    - branch: main

- service-module
    - description: import dependencies for a Dubbo service
    - url: https://github.com/CurtisNewbie/service-module
    - branch: main

- distributed-task-module
    - description: for distributed task scheduling
    - url: https://github.com/CurtisNewbie/distributed-task-module
    - branch: main