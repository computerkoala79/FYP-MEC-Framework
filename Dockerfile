FROM alpine:3.11
EXPOSE 8001
RUN apk update && apk add openjdk11 && apk add bash && apk add maven git
COPY . /app
WORKDIR /app/Orchestrator/target