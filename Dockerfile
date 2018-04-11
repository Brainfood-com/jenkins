FROM docker:stable-dind as src-docker

FROM docker/compose:1.20.0 as src-compose

FROM jenkins/jenkins:lts

COPY --from=src-compose /usr/local/bin/docker-compose /usr/local/bin/docker-compose
COPY --from=src-docker /usr/local/bin/docker /usr/local/bin/docker

USER root

RUN export DEBIAN_FRONTEND=noninteractive \
	&& apt-get update \
	&& apt-get install -y netcat \
	&& apt-get clean \
	&& find /var/cache/apt /var/lib/apt -type f -delete \
	&& addgroup --gid 999 docker \
	&& adduser jenkins docker \
	&& true

USER jenkins
COPY auto-configure.groovy /usr/share/jenkins/ref/init.groovy.d/auto-configure.groovy
