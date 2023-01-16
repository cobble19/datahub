package com.linkedin.datahub.graphql.resolvers.datasource;

import com.linkedin.datahub.graphql.generated.DatasourceSourceInput;
import com.linkedin.datahub.graphql.generated.DatasourceTestInput;
import com.linkedin.datasource.sources.*;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.bindArgument;
import static com.linkedin.datahub.graphql.resolvers.datasource.DatasourceConstants.*;

@Slf4j
public class TestDatasourceResolver implements DataFetcher<CompletableFuture<Boolean>>
{
    @Override
    public CompletableFuture<Boolean> get(DataFetchingEnvironment environment) throws Exception {

        Map<String, Object> inputMap = environment.getArgument("input");

        final DatasourceTestInput input = bindArgument(inputMap, DatasourceTestInput.class);
        DatasourceSourceInput sourceInput = input.getConnection();

        String type;
        Properties props = new Properties();
        if (sourceInput.getPostgres() != null) {
            type = POSTGRES_SOURCE_NAME;
            props.put("user", sourceInput.getPostgres().getUsername());
            props.put("password", sourceInput.getPostgres().getPassword());
            props.put("driver", new PostgresSource().getDriver());
        } else if (sourceInput.getOracle() != null) {
            type = ORACLE_SOURCE_NAME;
            props.put("user", sourceInput.getOracle().getUsername());
            props.put("password", sourceInput.getOracle().getPassword());
            props.put("driver", new OracleSource().getDriver());
        } else if (sourceInput.getMysql() != null) {
            type = MYSQL_SOURCE_NAME;
            props.put("user", sourceInput.getMysql().getUsername());
            props.put("password", sourceInput.getMysql().getPassword());
            props.put("driver", new MysqlSource().getDriver());
        } else if (sourceInput.getHive() != null) {
            type = HIVE_SOURCE_NAME;
            props.put("user", sourceInput.getHive().getUsername());
            props.put("password", sourceInput.getHive().getPassword());
            props.put("driver", new HiveSource().getDriver());
        } else if (sourceInput.getPinot() != null) {
            type = PINOT_SOURCE_NAME;
            props.put("user", sourceInput.getPinot().getUsername());
            props.put("password", sourceInput.getPinot().getPassword());
            props.put("driver", new PinotSource().getDriver());
        } else if (sourceInput.getPresto() != null) {
            type = PRESTO_SOURCE_NAME;
            props.put("user", sourceInput.getPresto().getUsername());
            props.put("password", sourceInput.getPresto().getPassword());
            props.put("driver", new PrestoSource().getDriver());
        } else if (sourceInput.getTrino() != null) {
            type = TRINO_SOURCE_NAME;
            props.put("user", sourceInput.getTrino().getUsername());
            props.put("password", sourceInput.getTrino().getPassword());
            props.put("driver", new TrinoSource().getDriver());
        } else if (sourceInput.getTiDB() != null) {
            type = TIDB_SOURCE_NAME;
            props.put("user", sourceInput.getTiDB().getUsername());
            props.put("password", sourceInput.getTiDB().getPassword());
            props.put("driver", new TiDBSource().getDriver());
        } else if (sourceInput.getSnowflake() != null) {
            type = SNOWFLAKE_SOURCE_NAME;
            props.put("user", sourceInput.getSnowflake().getUsername());
            props.put("password", sourceInput.getSnowflake().getPassword());
            props.put("driver", new SnowflakeSource().getDriver());
        } else if (sourceInput.getIceberg() != null) {
            type = ICEBERG_SOURCE_NAME;
        } else if (sourceInput.getKafka() != null) {
            type = KAFKA_SOURCE_NAME;
        } else {
            throw new IllegalArgumentException("Unknown source type");
        }

        if (supportType(type)) {

            final String jdbcUrl = parseJdbcUrl(type, sourceInput);

            return CompletableFuture.supplyAsync(() -> {
                try (Connection conn = DriverManager.getConnection(jdbcUrl, props);
                     Statement stat = conn.createStatement()) {
                    stat.executeQuery(input.getTestQuerySql());
                    return true;
                } catch (Exception ex) {
                    log.error("Failed to test datasource.", ex);
                    return false;
                }
            });

        }

        throw new IllegalArgumentException("Not support type:" + type);
    }

    private static String parseJdbcUrl(String type, DatasourceSourceInput sourceInput) {

        if (PINOT_SOURCE_NAME.equals(type)) {
            return "jdbc:" + JDBC_TYPES.get(type) + "://" + sourceInput.getPinot().getHostPort();
        } else if (TRINO_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getTrino().getHostPort(),
                    sourceInput.getTrino().getCatalog(),
                    sourceInput.getTrino().getSchema(),
                    sourceInput.getTrino().getJdbcParams()
            );
        } else if(PRESTO_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getPresto().getHostPort(),
                    sourceInput.getPresto().getCatalog(),
                    sourceInput.getPresto().getSchema(),
                    sourceInput.getPresto().getJdbcParams()
            );
        } else if (ORACLE_SOURCE_NAME.equals(type)) {
            String jdbcUrl = "jdbc:" + JDBC_TYPES.get(type) + ":@";
            String hostPort = sourceInput.getOracle().getHostPort();
            String serviceName = sourceInput.getOracle().getServiceName();
            if (StringUtils.isNotEmpty(hostPort) && StringUtils.isNotEmpty(serviceName)) {
                jdbcUrl += "//" + hostPort + "/" + serviceName;
            } else {
                jdbcUrl += sourceInput.getOracle().getTnsName();
            }
            return jdbcUrl;
        } else if (HIVE_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getHive().getHostPort(),
                    sourceInput.getHive().getDatabase(),
                    null,
                    sourceInput.getHive().getJdbcParams()
            );
        } else if (POSTGRES_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getPostgres().getHostPort(),
                    sourceInput.getPostgres().getDatabase(),
                    null,
                    sourceInput.getPostgres().getJdbcParams()
            );
        } else if (MYSQL_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getMysql().getHostPort(),
                    sourceInput.getMysql().getDatabase(),
                    null,
                    sourceInput.getMysql().getJdbcParams()
            );
        } else if (TIDB_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getTiDB().getHostPort(),
                    sourceInput.getTiDB().getDatabase(),
                    null,
                    sourceInput.getTiDB().getJdbcParams()
            );
        } else if (SNOWFLAKE_SOURCE_NAME.equals(type)) {
            String jdbcUrl = "jdbc:" + JDBC_TYPES.get(type) + "://" +
                    sourceInput.getSnowflake().getHostPort() + "/";
            String connParams = sourceInput.getSnowflake().getConnectionParams();
            if (StringUtils.isNotEmpty(connParams)) {
                jdbcUrl += connParams.startsWith("?") ? connParams : ("?" + connParams);
            }
            return jdbcUrl;
        }

        throw new IllegalArgumentException("Not support the type:" + type);
    }

    private static String urlFor(String type, String hostPort, String catalog, String schema, String jdbcParams) {
        String jdbcUrl = "jdbc:" + JDBC_TYPES.get(type) + "://" + hostPort;
        if (StringUtils.isNotEmpty(catalog)) {
            jdbcUrl += "/" + catalog;
            if (StringUtils.isNotEmpty(schema)) {
                jdbcUrl += "/" + schema;
            }
        }
        if (StringUtils.isNotEmpty(jdbcParams)) {
            jdbcParams = jdbcParams.startsWith("?") ? jdbcParams : ("?" + jdbcParams);
            jdbcUrl += jdbcParams;
        }
        return jdbcUrl;
    }

    private static boolean supportType(String type) {
        return HIVE_SOURCE_NAME.equals(type)
                || MYSQL_SOURCE_NAME.equals(type)
                || ORACLE_SOURCE_NAME.equals(type)
                || PINOT_SOURCE_NAME.equals(type)
                || POSTGRES_SOURCE_NAME.equals(type)
                || PRESTO_SOURCE_NAME.equals(type)
                || SNOWFLAKE_SOURCE_NAME.equals(type)
                || TIDB_SOURCE_NAME.equals(type)
                || TRINO_SOURCE_NAME.equals(type);
    }

}