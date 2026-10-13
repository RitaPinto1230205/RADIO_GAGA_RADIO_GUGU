FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 radiogaga
COPY --from=build /app/target/*.jar app.jar
USER radiogaga
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]