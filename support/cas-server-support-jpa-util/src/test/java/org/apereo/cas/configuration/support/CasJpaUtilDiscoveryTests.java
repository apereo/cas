package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.config.CasJpaUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.discovery.CasServerProfile;
import org.apereo.cas.discovery.CasServerProfileCustomizer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJpaUtilDiscoveryTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@SpringBootTest(classes = CasJpaUtilAutoConfiguration.class)
@SpringBootTestAutoConfigurations
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Hibernate")
@ExtendWith(CasTestExtension.class)
class CasJpaUtilDiscoveryTests {
    @Autowired
    @Qualifier("jpaUtilCasServerProfileCustomizer")
    private CasServerProfileCustomizer jpaUtilCasServerProfileCustomizer;

    @Test
    void verifyOperation() {
        val profile = new CasServerProfile();
        jpaUtilCasServerProfileCustomizer.customize(profile, new MockHttpServletRequest(), new MockHttpServletResponse());
        assertNotNull(profile.getDetails().get("jdbcInfo"));
    }
}
