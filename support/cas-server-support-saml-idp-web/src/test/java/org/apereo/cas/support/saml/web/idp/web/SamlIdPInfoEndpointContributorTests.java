package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPInfoEndpointContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("SAML2")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPInfoEndpointContributorTests {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Test
    void verifyOperation() throws Exception {
        val contributor = new SamlIdPInfoEndpointContributor(casProperties);
        val builder = new Info.Builder();
        contributor.contribute(builder);
        val info = builder.build();
        assertTrue(info.getDetails().containsKey("saml2"));
    }
}
