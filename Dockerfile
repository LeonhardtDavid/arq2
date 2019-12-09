FROM mozilla/sbt:8u212_1.2.8 AS BUILD
WORKDIR /app
COPY . /app
RUN sbt clean stage

FROM openjdk:8u212-jre-alpine
COPY --from=BUILD /app/target/universal/stage /app
CMD /app/bin/arq2
