# Chat-Service

Service for chatting, functionalities are mainly backed by Redis, messages are not persistent.

This app is ***not a standalone server***, it internally uses Dubbo for RPC to talk to other services (e..g, auth-service mentioned below). You must have `auth-service`  as well as other middlewares running to use it. To compile this app, you will also need to manually install the following modules & dependencies, these are all my repositories.

Two job beans are created that may run to cleanup some expired rooms:

- com.curtisnewbie.service.chat.job.ClearRoomJob
- com.curtisnewbie.service.chat.job.ClearExpiredPublicRoomListJob

These jobs are run by the `distributed-task-module`, you may schedule these two jobs by adding following two lines in the table (for more see the `distributed-task-module`):

|id |job_name      |target_bean |cron_expr    |app_group   |last_run_start_time|last_run_end_time  |last_run_by|enabled|concurrent_enabled|
|---|--------------|------------|-------------|------------|-------------------|-------------------|-----------|-------|------------------|
|2  |clear-room-job|clearRoomJob|0 0/1 * ? * *|chat-service|2021-08-25 15:35:00|2021-08-25 15:35:00|scheduler  |0      |0                 |
|3  |ClearExpiredPublicRoomListJob|clearExpiredPublicRoomListJob|0 0/1 * ? * *|chat-service|2021-08-26 15:06:00|2021-08-26 15:06:00|scheduler  |1      |0                 

## Related-Services

- auth-service 
    - description: service for managing users, access log and operation log.
    - url: https://github.com/CurtisNewbie/auth-service

## Middlewares

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
    - version: micro-0.0.1 (under `/microservce` folder)

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

- redis-util-module
    - description: Utility classes for Redis
    - url: https://github.com/CurtisNewbie/redis-util-module
    - branch: main

- distributed-task-module
    - description: for distributed task scheduling
    - url: https://github.com/CurtisNewbie/distributed-task-module
    - branch: main