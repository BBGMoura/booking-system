FROM eclipse-temurin:21-jre-jammy

EXPOSE 8080

ADD booking-system-app/target/booking-system-image-exec.jar booking-system-image.jar

ENTRYPOINT ["java", "-jar", "/booking-system-image.jar"]