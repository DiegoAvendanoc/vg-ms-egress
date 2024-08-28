FROM openjdk:17-jdk-alpine
WORKDIR /vg-ms-egress
COPY target/*.jar vg-ms-egress.jar
EXPOSE 7005
ENTRYPOINT ["java", "-jar", "vg-ms-egress.jar"]
