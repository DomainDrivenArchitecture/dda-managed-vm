FROM dda-managed-vm

RUN apt-get install curl -y

RUN curl -L -o /app/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/1.3.1/dda-serverspec-standalone.jar

COPY integration/docker/test/serverspec.edn /app/serverspec.edn

RUN java -jar /app/serverspec.jar /app/serverspec.edn
