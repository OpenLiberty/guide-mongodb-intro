# tag::stageOne[]
FROM open-liberty as staging

ARG COUNTRY=CA
ARG STATE_PROVINCE=Ontario
ARG LOCALITY=Markham
ARG ORGANIZATION=IBM
ARG UNIT=OpenLiberty
ARG COMMON_NAME=localhost

RUN openssl req -x509 -newkey rsa:2048 -nodes -days 365 \
    -keyout /tmp/private.key -out /tmp/cert.pem \
    -subj "/C=$COUNTRY/ST=$STATE_PROVINCE/L=$LOCALITY/O=$ORGANIZATION/OU=$UNIT/CN=$COMMON_NAME"
RUN cat /tmp/private.key /tmp/cert.pem > /tmp/mongodb_tls.pem

RUN keytool -import -trustcacerts -keystore /tmp/truststore.p12 \
    -storepass mongodb -storetype PKCS12 -alias mongo -file /tmp/cert.pem -noprompt
# end::stageOne[]
# tag::stageTwo[]
FROM mongo
RUN mkdir /etc/mongodb
COPY --from=staging /tmp /etc/mongodb
COPY assets/mongodb.conf  /etc/mongodb
# end::stageTwo[]