package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.discovery.CasServerProfileCustomizer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import lombok.val;
import oracle.jdbc.driver.OracleDriver;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasJpaUtilAutoConfiguration}.
 * This will import {@link DataSourceAutoConfiguration}
 * but only conditionally, if a datasource url is found in properties.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.JDBC)
@AutoConfiguration
public class CasJpaUtilAutoConfiguration {

    @Configuration(value = "CasJpaUtilDiscoveryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(CasServerProfileCustomizer.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Discovery)
    static class CasJpaUtilDiscoveryConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaUtilCasServerProfileCustomizer")
        @Bean
        public CasServerProfileCustomizer jpaUtilCasServerProfileCustomizer(
            final CasConfigurationProperties casProperties) {
            return (profile, request, response) -> {
                val drivers = List.of(
                    org.postgresql.Driver.class.getName(),
                    com.mysql.cj.jdbc.Driver.class.getName(),
                    OracleDriver.class.getName(),
                    SQLServerDriver.class.getName(),
                    org.mariadb.jdbc.Driver.class.getName()
                );
                val dialects = List.of(
                    PostgreSQLDialect.class.getName(),
                    MySQLDialect.class.getName(),
                    OracleDialect.class.getName(),
                    SQLServerDialect.class.getName(),
                    MariaDBDialect.class.getName()
                );
                profile.getDetails().put("jdbcInfo",
                    Map.of("drivers", drivers, "dialects", dialects));
            };
        }
    }
}
