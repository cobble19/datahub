#!/bin/bash

SSL_KEYSTORE_LOCATION=/etc/datahub/certs/server.keystore.bcfks
SSL_KEYSTORE_PASS=$(cat "/etc/datahub/certs/keystore.credentials")
SSL_TRUSTSTORE_LOCATION=/etc/datahub/certs/server.truststore.bcfks
SSL_TRUSTSTORE_PASS=$(cat "/etc/datahub/certs/truststore.credentials")

POSTGRES_PASS=$(cat "/etc/datahub/certs/postgres.credentials")
ELASTICSEARCH_PASS=$(cat "/etc/datahub/certs/elasticsearch.credentials")

MACHINE_ACCOUNT_PASS=$(cat "/etc/datahub/certs/machine.account.credentials")
CI_CLIENT_PASS=$(cat "/etc/datahub/certs/ci.client.credentials")

JAVA_OPTS="$JAVA_OPTS -Djavax.net.debug=all -Djavax.net.ssl.keyStore=$SSL_KEYSTORE_LOCATION \
    -Djavax.net.ssl.keyStorePassword=$SSL_KEYSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStore=$SSL_TRUSTSTORE_LOCATION \
    -Djavax.net.ssl.trustStorePassword=$SSL_TRUSTSTORE_PASS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2 \
    -Dssl.keystore.password=$SSL_KEYSTORE_PASS"

export SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL=SSL
export SPRING_KAFKA_PROPERTIES_SSL_KEYSTORE_LOCATION="$SSL_KEYSTORE_LOCATION"
export SPRING_KAFKA_PROPERTIES_SSL_KEYSTORE_PASSWORD="$SSL_KEYSTORE_PASS"
export SPRING_KAFKA_PROPERTIES_SSL_KEYSTORE_TYPE=BCFKS
export SPRING_KAFKA_PROPERTIES_SSL_TRUSTSTORE_LOCATION="$SSL_TRUSTSTORE_LOCATION"
export SPRING_KAFKA_PROPERTIES_SSL_TRUSTSTORE_PASSWORD="$SSL_TRUSTSTORE_PASS"
export SPRING_KAFKA_PROPERTIES_SSL_TRUSTSTORE_TYPE=BCFKS
export SPRING_KAFKA_PROPERTIES_SSL_ENABLED_PROTOCOLS=TLSv1.2

export EBEAN_DATASOURCE_PASSWORD="$POSTGRES_PASS"
export ELASTICSEARCH_PASSWORD="$ELASTICSEARCH_PASS"
export ES_PASSWORD="$ELASTICSEARCH_PASSWORD"
export MATS_CI_MACHINE_ACCOUNT_PASS="$MACHINE_ACCOUNT_PASS"
export MATS_CI_CLIENT_PASS="$CI_CLIENT_PASS"

# Add default URI (http) scheme if needed
if ! echo $NEO4J_HOST | grep -q "://" ; then
    NEO4J_HOST="http://$NEO4J_HOST"
fi

if [[ ! -z $ELASTICSEARCH_USERNAME ]] && [[ -z $ELASTICSEARCH_AUTH_HEADER ]]; then
  AUTH_TOKEN=$(echo -ne "$ELASTICSEARCH_USERNAME:$ELASTICSEARCH_PASSWORD" | base64 --wrap 0)
  ELASTICSEARCH_AUTH_HEADER="Authorization:Basic $AUTH_TOKEN"
fi

# Add default header if needed
if [[ -z $ELASTICSEARCH_AUTH_HEADER ]]; then
  ELASTICSEARCH_AUTH_HEADER="Accept: */*"
fi

if [[ $ELASTICSEARCH_USE_SSL == true ]]; then
  ELASTICSEARCH_PROTOCOL=https
else
  ELASTICSEARCH_PROTOCOL=http
fi

WAIT_FOR_EBEAN=""
if [[ $SKIP_EBEAN_CHECK != true ]]; then
  WAIT_FOR_EBEAN=" -wait tcp://$EBEAN_DATASOURCE_HOST "
fi

WAIT_FOR_KAFKA=""
if [[ $SKIP_KAFKA_CHECK != true ]]; then
  WAIT_FOR_KAFKA=" -wait tcp://$(echo $KAFKA_BOOTSTRAP_SERVER | sed 's/,/ -wait tcp:\/\//g') "
fi

WAIT_FOR_NEO4J=""
if [[ $GRAPH_SERVICE_IMPL != elasticsearch ]] && [[ $SKIP_NEO4J_CHECK != true ]]; then
  WAIT_FOR_NEO4J=" -wait $NEO4J_HOST "
fi

OTEL_AGENT=""
if [[ $ENABLE_OTEL == true ]]; then
  OTEL_AGENT="-javaagent:opentelemetry-javaagent-all.jar "
fi

PROMETHEUS_AGENT=""
if [[ $ENABLE_PROMETHEUS == true ]]; then
  PROMETHEUS_AGENT="-javaagent:jmx_prometheus_javaagent.jar=4318:/datahub/datahub-gms/scripts/prometheus-config.yaml "
fi

COMMON="
    $WAIT_FOR_EBEAN \
    $WAIT_FOR_KAFKA \
    $WAIT_FOR_NEO4J \
    -timeout 240s \
    $TOMCAT_HOME/bin/catalina.sh run"

if [[ $SKIP_ELASTICSEARCH_CHECK != true ]]; then
  dockerize \
    -wait $ELASTICSEARCH_PROTOCOL://$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT -wait-http-header "$ELASTICSEARCH_AUTH_HEADER" \
    $COMMON
else
  dockerize $COMMON
fi
