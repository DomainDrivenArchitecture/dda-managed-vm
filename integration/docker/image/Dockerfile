FROM ubuntu

RUN apt-get update && apt-get upgrade -y && apt-get install openjdk-8-jre -y

RUN apt-get install curl gnupg2 -y

COPY target/dda-managed-vm-standalone.jar /app/dda-managed-vm-standalone.jar
COPY integration/docker/image/vm.edn /app/vm.edn

RUN java -jar /app/dda-managed-vm-standalone.jar /app/vm.edn
