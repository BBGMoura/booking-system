FROM eclipse-temurin:21-jre-jammy

EXPOSE 8080

ENV JAVA_OPTS="-Xms220m -Xmx460m -XX:+UseParallelGC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

ADD target/booking-system-image.jar booking-system-image.jar

ENTRYPOINT ["java", "-jar","/booking-system-image.jar"]