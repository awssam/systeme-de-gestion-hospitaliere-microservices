FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
ARG MODULE
RUN chmod +x mvnw && ./mvnw clean package -pl ${MODULE} -am -DskipTests -B -q
RUN cp ${MODULE}/target/*.jar app.jar

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
