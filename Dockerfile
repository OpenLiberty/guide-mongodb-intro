# tag::stageOne[]
# tag::openLiberty[]
FROM adoptopenjdk/openjdk8-openj9:ubi as staging
# end::openLiberty[]
ARG COUNTRY=CA
ARG STATE_PROVINCE=Ontario
ARG LOCALITY=Markham
ARG ORGANIZATION=IBM
ARG UNIT=OpenLiberty
ARG COMMON_NAME=localhost
RUN openssl req -x509 -newkey rsa:2048 -nodes -days 365 \
    # tag::selfSignedCert[]
    -out /tmp/cert.pem \
    # end::selfSignedCert[]
    # tag::privateKey[]
    -keyout /tmp/private.key \
    # end::privateKey[]
    -subj "/C=$COUNTRY/ST=$STATE_PROVINCE/L=$LOCALITY/O=$ORGANIZATION/OU=$UNIT/CN=$COMMON_NAME"
# tag::catCerts[]
RUN cat /tmp/private.key /tmp/cert.pem > /tmp/mongodb_tls.pem
# end::catCerts[]
# tag::keytool[]
RUN keytool -import -trustcacerts -keystore /tmp/truststore.p12 \
    -storepass mongodb -storetype PKCS12 -alias mongo -file /tmp/cert.pem -noprompt
# end::keytool[]
# end::stageOne[]
# tag::stageTwo[]
# tag::mongo[]
FROM mongo
# end::mongo[]

# tag::createDirectory[]
RUN mkdir /etc/mongodb
RUN mkdir /etc/mongodb/data
RUN mkdir /etc/mongodb/certs
RUN mkdir /etc/mongodb/logs
RUN chown -R mongodb:mongodb /etc/mongodb
# end::createDirectory[]

# tag::copyCerts[]
COPY --from=staging /tmp /etc/mongodb/certs
# end::copyCerts[]
# tag::copyConfig[]
COPY assets/mongodb.conf  /etc/mongodb
# end::copyConfig[]
# tag::copyJS[]
COPY assets/index.js  /etc/mongodb
# end::copyJS[]

# tag:: runMongod[]
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
# end::tls[]
# tag::tlsCAFile[]
        --tlsCAFile /etc/mongodb/certs/cert.pem \
# end::tlsCAFile[]
# tag::host[]
        --host localhost \
# end::host[]
# tag::script[]
        /etc/mongodb/index.js \
# end::script[]
# end::runScript[]
# tag::shutdown[] 
    && mongod --dbpath /etc/mongodb/data --shutdown \
# end::shutdown[]
# tag::chown[]
    && chown -R mongodb /etc/mongodb
# end:: chown[]

# tag::cmd[]
CMD ["mongod", "--config", "/etc/mongodb/mongodb.conf"]
# end::cmd[]
# end::stageTwo[]
