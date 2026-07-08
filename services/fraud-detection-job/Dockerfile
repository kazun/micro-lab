# Build the jar on the host first:
#   cd services && mvn -pl fraud-detection-job clean package -DskipTests
# Then build from the services/ directory:
#   docker build -f fraud-detection-job/Dockerfile -t peap/fraud-detection-job:dev .
#
# The same image runs both Flink roles; the container command decides:
#   jobmanager:   standalone-job --job-classname com.peap.fraud.FraudDetectionJob
#   taskmanager:  taskmanager

FROM flink:1.20-java17
COPY fraud-detection-job/target/fraud-detection-job-*.jar /opt/flink/usrlib/fraud-detection-job.jar
