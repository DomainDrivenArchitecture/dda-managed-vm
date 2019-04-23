FROM ubuntu

RUN apt update -y > /dev/null
RUN apt upgrade -y  > /dev/null
RUN apt install curl gnupg2 apt-utils sudo openjdk-11-jdk -y  > /dev/null

COPY target/uberjar/dda-managed-vm-standalone.jar /app/dda-managed-vm-standalone.jar
COPY integration/docker/image/vm.edn /app/vm.edn

# check prerequisits
RUN java -jar /app/dda-managed-vm-standalone.jar --serverspec /app/vm.edn

# provision
#RUN java -jar /app/dda-managed-vm-standalone.jar /app/vm.edn
