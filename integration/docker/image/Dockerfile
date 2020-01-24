FROM ubuntu:18.04

RUN apt-get -qqy update > /dev/null
RUN apt-get -qqy upgrade > /dev/null
RUN apt-get -qqy install curl gnupg2 apt-utils sudo openjdk-11-jdk > /dev/null

COPY target/uberjar/dda-managed-vm-standalone.jar /app/dda-managed-vm-standalone.jar
COPY integration/docker/image/vm.edn /app/vm.edn

# check prerequisits
RUN java -jar /app/dda-managed-vm-standalone.jar --serverspec /app/vm.edn

# provision
RUN java -jar /app/dda-managed-vm-standalone.jar /app/vm.edn
