FROM mongo
COPY ./assets /etc/mongodb
COPY ./assets/mongod.conf /root/.docker
EXPOSE 27017:tcp