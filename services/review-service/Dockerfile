# Build the jar on the host first (fast, uses your local ~/.m2 cache):
#   cd services && mvn -pl review-service -am clean package -DskipTests
# Then build from the services/ directory:
#   docker build -f review-service/Dockerfile -t peap/review-service:dev .

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY review-service/target/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
