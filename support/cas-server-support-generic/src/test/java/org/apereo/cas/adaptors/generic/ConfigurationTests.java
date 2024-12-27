package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasGenericAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreLogoutAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasGenericAuthenticationAutoConfiguration.class
}, properties = {
    "cas.authn.file.filename=classpath:authentication.txt",
    "cas.authn.groovy.location=classpath:GroovyAuthnHandler.groovy",
    "cas.authn.json.location=classpath:sample-users.json",
    "cas.authn.reject.users=one,two,three"
})
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class ConfigurationTests {
    @Autowired
    @Qualifier("fileAuthenticationHandler")
    private AuthenticationHandler fileAuthenticationHandler;

    @Autowired
    @Qualifier("groovyResourceAuthenticationHandler")
    private AuthenticationHandler groovyResourceAuthenticationHandler;

    @Autowired
    @Qualifier("jsonResourceAuthenticationHandler")
    private AuthenticationHandler jsonResourceAuthenticationHandler;

    @Autowired
    @Qualifier("rejectUsersAuthenticationHandler")
    private AuthenticationHandler rejectUsersAuthenticationHandler;

    @Test
    void verifyOperation() {
        assertNotNull(fileAuthenticationHandler);
        assertNotNull(groovyResourceAuthenticationHandler);
        assertNotNull(jsonResourceAuthenticationHandler);
        assertNotNull(rejectUsersAuthenticationHandler);
    }
}
