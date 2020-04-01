# tag::stageOne[]
# tag::openLiberty[]
FROM open-liberty as staging
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
# end::createDirectory[]

# tag::copyCerts[]
COPY --from=staging /tmp /etc/mongodb
# end::copyCerts[]
# tag::copyConfig[]
COPY assets/mongodb.conf  /etc/mongodb
# tag::copyConfig[]
# end::stageTwo[]