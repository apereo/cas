package org.apereo.cas.web.saml2;

import org.apereo.cas.config.DelegatedAuthenticationSAMLConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedSaml2ClientMetadataJdbcTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    DelegatedAuthenticationSAMLConfiguration.class,
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
},
    properties = {
        "CasFeatureModule.DelegatedAuthentication.saml-jdbc.enabled=true",

        "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.auto-commit=true",
        "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.table-name=sp_metadata",
        "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.url=jdbc:postgresql://localhost:5432/saml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.user=postgres",
        "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.password=password",
        "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.driver-class=org.postgresql.Driver",
        "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.dialect=org.hibernate.dialect.PostgreSQLDialect",

        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=src/test/resources/idp-metadata.xml"
    })
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedSaml2ClientMetadataJdbcTests {
    @Autowired
    @Qualifier("delegatedSaml2ClientMetadataController")
    private DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController;


    @BeforeAll
    public static void setUp() {
        var dataSource = JpaBeans.newDataSource("org.postgresql.Driver",
            "postgres", "password", "jdbc:postgresql://localhost:5432/saml");
        var template = new JdbcTemplate(dataSource);
        template.execute("DROP TABLE IF EXISTS sp_metadata");
        template.execute("CREATE TABLE sp_metadata (entityId VARCHAR(255), metadata TEXT)");
    }
    
    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(delegatedSaml2ClientMetadataController.getFirstServiceProviderMetadata());
        assertTrue(delegatedSaml2ClientMetadataController.getServiceProviderMetadataByName("SAML2Client").getStatusCode().is2xxSuccessful());
    }
}
