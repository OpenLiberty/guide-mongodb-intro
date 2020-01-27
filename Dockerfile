FROM mongo
COPY ./assets /etc/mongodb
RUN chown -R mongodb:mongodb /etc/mongodb/storage
