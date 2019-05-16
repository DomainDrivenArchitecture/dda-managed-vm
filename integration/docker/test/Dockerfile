FROM dda-managed-vm

RUN apt-get -qqy install curl

RUN curl -L -o /app/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/1.3.4/dda-serverspec-standalone.jar

COPY integration/docker/test/serverspec.edn /app/serverspec.edn

RUN java -jar /app/serverspec.jar /app/serverspec.edn
