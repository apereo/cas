FROM node:20.7.0
RUN yarn global add @strapi/strapi@4.13.7
RUN mkdir /srv/app && chown 1000:1000 -R /srv/app
WORKDIR /srv/app

RUN strapi version && \
    strapi new . \
      --dbclient=sqlite \
      --dbhost=$DATABASE_HOST \
      --dbport=$DATABASE_PORT \
      --dbname=$DATABASE_NAME \
      --dbusername=$DATABASE_USERNAME \
      --dbpassword=$DATABASE_PASSWORD \
      --dbssl=$DATABASE_SSL

COPY config.json .
COPY server.js ./config

COPY entrypoint.sh /usr/local/bin/
RUN chmod 777 /usr/local/bin/entrypoint.sh
ENTRYPOINT ["entrypoint.sh"]
