package org.apereo.cas.support.pac4j.clients;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.clients.JdbcDelegatedIdentityProviderFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcDelegatedIdentityProviderFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("JDBC")
@SpringBootTest(classes = {
    JdbcDelegatedIdentityProviderFactoryTests.JdbcTestConfiguration.class,
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.pac4j.jdbc.autocommit=true",
        "CasFeatureModule.DelegatedAuthentication.jdbc.enabled=true"
    })
@ExtendWith(CasTestExtension.class)
class JdbcDelegatedIdentityProviderFactoryTests {
    @Autowired
    @Qualifier("pac4jDelegatedClientFactory")
    private DelegatedIdentityProviderFactory pac4jDelegatedClientFactory;


    @Test
    void verifyOperation() {
        val clientList = pac4jDelegatedClientFactory.build();
        assertFalse(clientList.isEmpty());
    }

    @TestConfiguration(value = "JdbcTestConfiguration", proxyBeanMethods = false)
    static class JdbcTestConfiguration {
        @Bean
        public InitializingBean createTables(
            @Qualifier("pac4jDelegatedClientJdbcTemplate") final JdbcOperations pac4jDelegatedClientJdbcTemplate,
            @Qualifier("pac4jDelegatedClientDataSource") final DataSource pac4jDelegatedClientDataSource) {
            return () -> {
                val create = """
                    CREATE TABLE %s (
                        id INTEGER IDENTITY PRIMARY KEY,
                        type VARCHAR(250),
                        index INTEGER,
                        name VARCHAR(250),
                        value VARCHAR(250)
                    );
                    """.formatted(JdbcDelegatedIdentityProviderFactory.JdbcIdentityProviderEntity.TABLE_NAME).stripIndent().stripLeading();
                pac4jDelegatedClientJdbcTemplate.execute(create);
                
                val insert = """
                    INSERT INTO $table (type, index, name, value) VALUES ('cas', 0, 'login-url', 'https://cas.example.org');
                    INSERT INTO $table (type, index, name, value) VALUES ('cas', 0, 'protocol', 'CAS20');
                    
                    INSERT INTO $table (type, index, name, value) VALUES ('saml', 0, 'client-name', 'SAML2Client');
                    
                    INSERT INTO $table (type, index, name, value) VALUES ('oidc', 0, 'generic.client-name', 'OidcClient');
                    
                    INSERT INTO $table (type, index, name, value) VALUES ('oauth2', 0, 'client-name', 'OAuth2Client');
                    
                    """.replace("$table", JdbcDelegatedIdentityProviderFactory.JdbcIdentityProviderEntity.TABLE_NAME)
                    .stripIndent().stripLeading();
                pac4jDelegatedClientJdbcTemplate.execute(insert);

            };
        }
    }
}
