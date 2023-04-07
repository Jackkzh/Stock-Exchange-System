FROM openjdk:17-jdk-slim
RUN mkdir /app
WORKDIR /app
#ARG JAR_FILE=Stock_Exchange_System-1.0-SNAPSHOT.jar
COPY ./build/libs/Stock_Exchange_System-1.0-SNAPSHOT.jar Stock_Exchange_System-1.0-SNAPSHOT.jar
ENTRYPOINT ["sh","-c","sleep 3 && java -jar Stock_Exchange_System-1.0-SNAPSHOT.jar"]
#ENTRYPOINT ["java", "-cp", "/app/Stock_Exchange_System-1.0-SNAPSHOT.jar"]

