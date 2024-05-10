package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link SimpleSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Tag("Impersonation")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SimpleSurrogateAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    private final SurrogateAuthenticationService service = new SimpleSurrogateAuthenticationService(
        CollectionUtils.wrap(
            "casuser", CollectionUtils.wrapList("banderson"),
            "casadmin", CollectionUtils.wrapList(SurrogateAuthenticationService.WILDCARD_ACCOUNT)
        ), servicesManager, casProperties);
}
