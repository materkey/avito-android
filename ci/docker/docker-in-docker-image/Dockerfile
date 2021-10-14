ARG DOCKER_REGISTRY=DOCKER_REGISTRY
# TODO: Use public alpine based image
FROM debian

WORKDIR /

ENV PYTHONUNBUFFERED 1
ENV PYTHONDONTWRITEBYTECODE 1

RUN apt-get update && \
	apt-get install -y apt-transport-https gnupg \
                       ca-certificates \
                       curl \
                       software-properties-common && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - && \
    add-apt-repository \
       "deb [arch=amd64] https://download.docker.com/linux/debian \
       $(lsb_release -cs) \
       stable"

RUN apt-get install -y make pip

# TODO: --no-install-recommends
RUN apt-get update && \
    apt-get install -y docker-ce && \
    apt-get clean && apt-get purge

COPY publish-docker-image publish-docker-image

RUN pip install setuptools

RUN make -C publish-docker-image install && \
    rm -rf publish-docker-image

COPY _conftest.py conftest.py
