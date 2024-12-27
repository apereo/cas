package org.apereo.cas;

import org.apereo.cas.config.CasJdbcCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.Serial;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJdbcCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasJdbcCloudConfigBootstrapAutoConfiguration.class)
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
class CasJdbcCloudConfigBootstrapAutoConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() throws Exception {
        val jpa = new Jpa();
        val ds = JpaBeans.newDataSource(jpa);
        try (val connection = ds.getConnection();
             val statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.execute("create table CAS_SETTINGS_TABLE (id VARCHAR(255), name VARCHAR(255), value VARCHAR(255));");
            statement.execute("insert into CAS_SETTINGS_TABLE (id, name, value) values('1', 'cas.authn.accept.users', '" + STATIC_AUTHN_USERS + "');");
        }
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }

    static class Jpa extends AbstractJpaProperties {
        @Serial
        private static final long serialVersionUID = 1210163210567513705L;
    }
}
