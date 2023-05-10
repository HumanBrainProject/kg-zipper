FROM openjdk:11-jre-slim
VOLUME /tmp
COPY target/*.jar kg-zipper.jar
ENV JAVA_OPTS=""
# Add a new user for the execution of the service
RUN useradd -u 42100 kg-zipper
# Change to non-root privilege
USER kg-zipper
ENTRYPOINT exec java $JAVA_OPTS -jar /kg-zipper.jar
