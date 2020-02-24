# scala-frontend-experiment
Learning project to try different Scala-based fontend approaches. Starting point of project: Akka Http in backend (this not going to be changed), HTML and JS assets in frontend. 

This project has two milestones: 
* Static HTML, JS files generated from ScalaJS
* ReactJS froneted implemented with ScalaJS

### Brief description of project functionality ###
Service for adding tags to tracks. Track list fetched from external service. Tags persisted in MySql database via Slick. User can create/delete new tag an assign any number of tags to track.

## Tech info ##

### Run ###
```sh
sbt modulesJVM/run
```

### DB migrations ###
Located in "/resources/db.migrations" dir. Applied on every start of application.

### Run MySql from Docker ###
```sh
docker-compose -f mysql.yml up
```

### Avoiding CORS restrictions for testing locally ###
Use CORS managing plugin for your browser. For example "CORS Unblock" for Chrome ( https://chrome.google.com/webstore/detail/cors-unblock/lfhmikememgdcahcdlaciloancbhjino?hl=en )

### Building and running docker container ###
Command to build and publish docker container:
```sh
sbt docker:publishLocal
```
Before running app from container you need to set MySQL host to "host.docker.internal" to have access to host's ports. 
Or you can substitute configurated value via env parameter:  
```sh
docker run --rm -p8090:8090 -v `pwd`/log:/opt/docker/log --env CONFIG_FORCE_slick_db_url="jdbc:mysql://host.docker.internal" scala-frontend-experiment:0.1.0-SNAPSHOT
```
