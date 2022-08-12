package com.linkedin.gms.factory.entity;

import com.linkedin.metadata.entity.ebean.EbeanAspectV2;
import io.ebean.EbeanServer;
import io.ebean.config.ServerConfig;
import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.sql.*;
import java.util.Properties;


@Configuration
@Slf4j
public class EbeanServerFactory {
  public static final String EBEAN_MODEL_PACKAGE = EbeanAspectV2.class.getPackage().getName();

  @Autowired
  ApplicationContext applicationContext;

  @Bean(name = "ebeanServer")
  @DependsOn({"gmsEbeanServiceConfig"})
  @Nonnull
  protected EbeanServer createServer() {
    ServerConfig serverConfig = applicationContext.getBean(ServerConfig.class);
    // Make sure that the serverConfig includes the package that contains DAO's Ebean model.
    if (!serverConfig.getPackages().contains(EBEAN_MODEL_PACKAGE)) {
      serverConfig.getPackages().add(EBEAN_MODEL_PACKAGE);
    }
    // TODO: Consider supporting SCSI

    testConnection(serverConfig);
    return io.ebean.EbeanServerFactory.create(serverConfig);
  }

  public void testConnection(ServerConfig serverConfig) {

    Properties props = new Properties();
    props.setProperty("user",serverConfig.getDataSourceConfig().getUsername());
    props.setProperty("password",serverConfig.getDataSourceConfig().getPassword());

    Connection conn = null;
    try {
      ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
      if (contextLoader != null) {
        Class.forName(serverConfig.getDataSourceConfig().getDriver(), true, contextLoader);
      } else {
        Class.forName(serverConfig.getDataSourceConfig().getDriver(), true, this.getClass().getClassLoader());
      }
      conn = DriverManager.getConnection(serverConfig.getDataSourceConfig().getUrl(), props);
      PreparedStatement stmtForCheckSSL = conn.prepareStatement("select 1");
      ResultSet rs = stmtForCheckSSL.executeQuery();
      if (rs.next()) {
        log.info("ssl_is_used: " + rs.getString(1));
      }
    } catch (Exception ex) {
      log.error("Failed to test database connection [" + serverConfig.getDataSourceConfig().getUrl() + "]", ex);
      try {
        if(conn != null) {
          conn.close();
        }
      } catch (Exception ignored){}
    }

  }
}
