#!/bin/bash

JAVA_OPTS="$JAVA_OPTS \
    -Djavax.net.ssl.keyStore=/etc/datahub/certs/server.keystore.bcfks \
    -Djavax.net.ssl.keyStorePassword=$SSL_KEYSTORE_PASS \
    -Djavax.net.ssl.trustStore=/etc/datahub/certs/server.truststore.bcfks \
    -Djavax.net.ssl.trustStorePassword=$SSL_TRUSTSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

export ZOOKEEPER_SERVER_CNXN_FACTORY=org.apache.zookeeper.server.NettyServerCnxnFactory
export ZOOKEEPER_SSL_KEYSTORE_LOCATION=/etc/datahub/certs/server.keystore.bcfks
export ZOOKEEPER_SSL_KEYSTORE_PASSWORD=$SSL_KEYSTORE_PASS
export ZOOKEEPER_SSL_KEYSTORE_TYPE=BCFIPS
export ZOOKEEPER_SSL_TRUSTSTORE_LOCATION=/etc/datahub/certs/server.truststore.bcfks
export ZOOKEEPER_SSL_TRUSTSTORE_PASSWORD=$SSL_TRUSTSTORE_PASS
export ZOOKEEPER_SSL_TRUSTSTORE_TYPE=BCFIPS
export ZOOKEEPER_SSL_ENABLED_PROTOCOLS=TLSv1.2
export ZOOKEEPER_SSL_QUORUM_KEYSTORE_LOCATION=/etc/datahub/certs/server.keystore.bcfks
export ZOOKEEPER_SSL_QUORUM_KEYSTORE_PASSWORD=$SSL_KEYSTORE_PASS
export ZOOKEEPER_SSL_QUORUM_KEYSTORE_TYPE=BCFIPS
export ZOOKEEPER_SSL_QUORUM_TRUSTSTORE_LOCATION=/etc/datahub/certs/server.truststore.bcfks
export ZOOKEEPER_SSL_QUORUM_TRUSTSTORE_PASSWORD=$SSL_TRUSTSTORE_PASS
export ZOOKEEPER_SSL_QUORUM_TRUSTSTORE_TYPE=BCFIPS
export ZOOKEEPER_SSL_QUORUM_ENABLED_PROTOCOLS=TLSv1.2

exec /etc/confluent/docker/run