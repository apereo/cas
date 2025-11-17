FROM node:22
RUN apt-get update > /dev/null \
    && apt-get install git patch iproute2 iputils-ping -y > /dev/null \
    && rm -rf /var/lib/apt/lists/*

ENV COREPACK_ENABLE_DOWNLOAD_PROMPT=0
RUN corepack enable

RUN mkdir /srv/app && chown 1000:1000 -R /srv/app
WORKDIR /srv/app
ENV NODE_ENV=development

RUN npm i -g patch-package

RUN npx create-strapi@5.23.0 . \
            --no-run \
            --use-yarn \
            --javascript \
            --install \
            --git-init \
            --example \
            --skip-cloud \
            --dbclient=sqlite \
            --dbfile=data.db

ENV ADMIN_JWT_SECRET=example-token
COPY config.json .
COPY server.js ./config

ENV PATH=$PATH:/srv/app/node_modules/.bin

COPY entrypoint.sh /usr/local/bin/
RUN chmod 777 /usr/local/bin/entrypoint.sh
ENTRYPOINT ["entrypoint.sh"]
