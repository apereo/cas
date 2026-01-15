package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.session.SessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.JdbcHttpSessionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJdbcSessionConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasJdbcSessionConfigurationTests.TransactionTestConfiguration.class,
    JdbcHttpSessionConfiguration.class,
    CasJdbcSessionAutoConfiguration.class
}, properties = {
    "spring.datasource.url=jdbc:hsqldb:mem:cas-hsql-database",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.hsqldb.jdbcDriver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.HSQLDialect",

    "spring.session.store-type=JDBC",
    "spring.session.jdbc.schema=classpath:org/springframework/session/jdbc/schema-@@platform@@.sql",
    "spring.session.jdbc.initialize-schema=always"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasJdbcSessionConfigurationTests {
    @Autowired
    @Qualifier("sessionRepository")
    private SessionRepository sessionRepository;

    @Test
    void verifyOperation() {
        assertNotNull(sessionRepository);
    }

    @TestConfiguration(value = "TransactionTestConfiguration", proxyBeanMethods = false)
    static class TransactionTestConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerYubiKey() {
            return new PseudoTransactionManager();
        }
    }
}
