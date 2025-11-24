FROM bellsoft/liberica-openjdk-alpine:21
WORKDIR /
VOLUME [ "/data" ]
COPY l9g-uidgen.jar /l9g-uidgen.jar
ENTRYPOINT ["java", "-jar", "/l9g-uidgen.jar"]
EXPOSE 8080
