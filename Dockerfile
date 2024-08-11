FROM amazoncorretto:17

COPY backend/build/libs/chongdae-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=${PROFILE}

ENTRYPOINT ["java", "-jar", "/app.jar"]
