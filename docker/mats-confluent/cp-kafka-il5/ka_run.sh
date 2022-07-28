#!/bin/bash

export KAFKA_SSL_KEYSTORE_TYPE=BCFKS
export KAFKA_SSL_ENABLED_PROTOCOLS=TLSv1.2
export KAFKA_SSL_TRUSTSTORE_TYPE=BCFKS
export KAFKA_SECURITY_INTER_BROKER_PROTOCOL=SSL

export KAFKA_SSL_KEY_CREDENTIALS=key.credentials
export KAFKA_SSL_KEYSTORE_FILENAME=server.keystore.bcfks
export KAFKA_SSL_KEYSTORE_CREDENTIALS=keystore.credentials
export KAFKA_SSL_TRUSTSTORE_FILENAME=server.truststore.bcfks
export KAFKA_SSL_TRUSTSTORE_CREDENTIALS=truststore.credentials

export KAFKA_DATA_DIRS=/var/kafka/data

SSL_KEYSTORE_LOCATION=/etc/kafka/secrets/server.keystore.bcfks
SSL_KEYSTORE_PASS=$(cat "/etc/kafka/secrets/keystore.credentials")
SSL_TRUSTSTORE_LOCATION=/etc/kafka/secrets/server.truststore.bcfks
SSL_TRUSTSTORE_PASS=$(cat "/etc/kafka/secrets/truststore.credentials")

JAVA_OPTS="$JAVA_OPTS \
    -Djavax.net.ssl.keyStore=$SSL_KEYSTORE_LOCATION \
    -Djavax.net.ssl.keyStorePassword=$SSL_KEYSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStore=$SSL_TRUSTSTORE_LOCATION \
    -Djavax.net.ssl.trustStorePassword=$SSL_TRUSTSTORE_PASS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

export KAFKA_ZOOKEEPER_CLIENT_CNXN_SOCKET=org.apache.zookeeper.ClientCnxnSocketNetty
export KAFKA_ZOOKEEPER_SSL_CLIENT_ENABLE=true
export KAFKA_ZOOKEEPER_SSL_KEYSTORE_LOCATION=$SSL_KEYSTORE_LOCATION
export KAFKA_ZOOKEEPER_SSL_KEYSTORE_PASSWORD="$SSL_KEYSTORE_PASS"
export KAFKA_ZOOKEEPER_SSL_KEYSTORE_TYPE=BCFKS
export KAFKA_ZOOKEEPER_SSL_TRUSTSTORE_LOCATION=$SSL_TRUSTSTORE_LOCATION
export KAFKA_ZOOKEEPER_SSL_TRUSTSTORE_PASSWORD="$SSL_TRUSTSTORE_PASS"
export KAFKA_ZOOKEEPER_SSL_TRUSTSTORE_TYPE=BCFKS

exec /etc/confluent/docker/run