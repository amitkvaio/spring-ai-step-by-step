FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir /app/data && chown -R 10001:10001 /app
COPY --chown=10001:10001 target/23-production-1.0.0.jar /app/app.jar
USER 10001:10001
ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8103
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","/app/app.jar"]
