FROM eclipse-temurin:21-jre-jammy

EXPOSE 8080

ADD target/booking-system-image.jar booking-system-image.jar

ENTRYPOINT ["java", "-jar","/booking-system-image.jar"]