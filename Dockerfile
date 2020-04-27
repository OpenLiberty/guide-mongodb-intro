# tag::stageOne[]
# tag::openLiberty[]
FROM adoptopenjdk/openjdk8-openj9:ubi as staging
# end::openLiberty[]

# Define the variables for the OpenSSL subject.
ARG COUNTRY=CA
ARG STATE=Ontario
ARG LOCALITY=Markham
ARG ORG=IBM
ARG UNIT=OpenLiberty
ARG COMMON_NAME=localhost

# Generate a self-signed certificate and private key.
RUN openssl req -x509 -newkey rsa:2048 -nodes -days 365 \
    # tag::selfSignedCert[]
    -out /tmp/cert.pem \
    # end::selfSignedCert[]
    # tag::privateKey[]
    -keyout /tmp/private.key \
    # end::privateKey[]
    -subj "/C=$COUNTRY/ST=$STATE/L=$LOCALITY/O=$ORG/OU=$UNIT/CN=$COMMON_NAME"

# Combine the self-signed certificate and private key.
# tag::catCerts[]
RUN cat /tmp/private.key /tmp/cert.pem > /tmp/mongodb_tls.pem
# end::catCerts[]

# Import combined file to truststore.
# tag::keytool[]
RUN keytool -import -trustcacerts -keystore /tmp/truststore.p12 \
    -storepass mongodb -storetype PKCS12 -alias mongo -file /tmp/cert.pem -noprompt
# end::keytool[]
# end::stageOne[]

# tag::stageTwo[]
# tag::mongo[]
FROM mongo
# end::mongo[]

# Create the directories.
# tag::createDirectory[]
RUN mkdir /home/mongodb
RUN mkdir /home/mongodb/data
RUN mkdir /home/mongodb/certs
RUN mkdir /home/mongodb/logs
RUN chown -R mongodb:mongodb /home/mongodb
# end::createDirectory[]

# Copy the certificates over from stage one.
# tag::copyCerts[]
COPY --from=staging /tmp /home/mongodb/certs
# end::copyCerts[]

# Copy the configuration and setup files from
# the host machine to the image.
# tag::copyConfig[]
COPY assets/mongodb.conf /home/mongodb
# end::copyConfig[]
# tag::copyJS[]
COPY assets/index.js /home/mongodb
# end::copyJS[]

# Run MongodDB daemon and execute the setup script.
# tag::runMongod[]
RUN mongod \
# tag::fork[]
        --fork \
# end::fork[]
# tag::config[]
        --config /home/mongodb/mongodb.conf \
# end::config[]
# end::runMongod[]
# tag::runScript[]
    && mongo \
# tag::testdb[]
        testdb \
# end::testdb[]
# tag::tls[]
        -tls \
        --tlsCAFile /home/mongodb/certs/cert.pem \
        --host localhost \
# end::tls[]
# tag::script[]
        /home/mongodb/index.js \
# end::script[]
# end::runScript[]
# tag::shutdown[] 
    && mongod --dbpath /home/mongodb/data --shutdown \
# end::shutdown[]
# tag::chown[]
    && chown -R mongodb /home/mongodb
# end::chown[]

# Start MongoDB daemon when the image is run in a container.
# tag::cmd[]
CMD ["mongod", "--config", "/home/mongodb/mongodb.conf"]
# end::cmd[]
# end::stageTwo[]
