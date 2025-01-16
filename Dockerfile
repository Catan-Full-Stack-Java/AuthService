FROM openjdk:17-alpine

WORKDIR
/app

COPY target/scala-2.13/akka-http-quickstart-assembly-0.1.jar

EXPOSE 5500

ENTRYPOINT ["java", "-jar", "app.jar"]