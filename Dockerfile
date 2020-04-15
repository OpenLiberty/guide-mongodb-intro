# tag::stageOne[]
# tag::openLiberty[]
FROM adoptopenjdk/openjdk8-openj9:ubi as staging
# end::openLiberty[]

# Define the variables for the OpenSSL subject.
ARG COUNTRY=CA
ARG PROVINCE=Ontario
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
    -subj "/C=$COUNTRY/ST=$PROVINCE/L=$LOCALITY/O=$ORG/OU=$UNIT/CN=$COMMON_NAME"

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
RUN mkdir /etc/mongodb
RUN mkdir /etc/mongodb/data
RUN mkdir /etc/mongodb/certs
RUN mkdir /etc/mongodb/logs
RUN chown -R mongodb:mongodb /etc/mongodb
# end::createDirectory[]

# Copy the certificates over from stage one.
# tag::copyCerts[]
COPY --from=staging /tmp /etc/mongodb/certs
# end::copyCerts[]

# Copy the configuration and setup files from
# the host machine to the image.
# tag::copyConfig[]
COPY assets/mongodb.conf /etc/mongodb
# end::copyConfig[]
# tag::copyJS[]
COPY assets/index.js /etc/mongodb
# end::copyJS[]

# Run MongodDB daemon and execute the setup script.
# tag::runMongod[]
RUN mongod \
# tag::fork[]
        --fork \
# end::fork[]
# tag::config[]
        --config /etc/mongodb/mongodb.conf \
# end::config[]
# end::runMongod[]
# tag::runScript[]
    && mongo \
# tag::testdb[]
        testdb \
# end::testdb[]
# tag::tls[]
        -tls \
        --tlsCAFile /etc/mongodb/certs/cert.pem \
        --host localhost \
# end::tls[]
# tag::script[]
        /etc/mongodb/index.js \
# end::script[]
# end::runScript[]
# tag::shutdown[] 
    && mongod --dbpath /etc/mongodb/data --shutdown \
# end::shutdown[]
# tag::chown[]
    && chown -R mongodb /etc/mongodb
# end::chown[]

# Start MongoDB daemon when the image is run in a container.
# tag::cmd[]
CMD ["mongod", "--config", "/etc/mongodb/mongodb.conf"]
# end::cmd[]
# end::stageTwo[]
