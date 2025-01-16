FROM        public.ecr.aws/docker/library/eclipse-temurin:21-jdk-jammy as build
COPY        . /tmp
WORKDIR     /tmp
RUN         ./gradlew bootJar -x test -x integrationTest

FROM        public.ecr.aws/docker/library/eclipse-temurin:21-jre-jammy
ENV         NAME_JAR="kafka-stream"
ENV         packageFile="/app/${NAME_JAR}.jar"
RUN         mkdir /app
WORKDIR     /app
COPY        --from=build /tmp/build/libs/${NAME_JAR}-*-boot.jar /app/${NAME_JAR}.jar
ENTRYPOINT  java -jar -Dfile.encoding="UTF-8" -Dspring.profiles.active=${PROFILE} ${packageFile}
