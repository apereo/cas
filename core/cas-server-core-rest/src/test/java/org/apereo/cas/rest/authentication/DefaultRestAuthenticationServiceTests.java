package org.apereo.cas.rest.authentication;

import module java.base;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreRestAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.LinkedMultiValueMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRestAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreRestAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    DefaultRestAuthenticationServiceTests.AuthenticationTestConfiguration.class,
    CasCoreWebflowAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultRestAuthenticationServiceTests {
    @Autowired
    @Qualifier(RestAuthenticationService.DEFAULT_BEAN_NAME)
    private RestAuthenticationService restAuthenticationService;

    @Test
    void verifyAuthentication() throws Throwable {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("195.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        
        val body = new LinkedMultiValueMap<String, String>();
        body.add(RestHttpRequestCredentialFactory.PARAMETER_USERNAME, "casuser");
        body.add(RestHttpRequestCredentialFactory.PARAMETER_PASSWORD, "Mellon");
        val result = restAuthenticationService.authenticate(body, request, response).get();
        assertEquals("casuser", result.getAuthentication().getPrincipal().getId());
    }

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }

        @Bean
        public PersonAttributeDao attributeRepository() {
            val attrs = CollectionUtils.wrap(
                "uid", CollectionUtils.wrap("uid"),
                "mail", CollectionUtils.wrap("cas@apereo.org"),
                "eduPersonAffiliation", CollectionUtils.wrap("developer"),
                "groupMembership", CollectionUtils.wrap("adopters"));
            return new StubPersonAttributeDao((Map) attrs);
        }

        @Bean
        public AttributeRepositoryResolver attributeRepositoryResolver() {
            return AttributeRepositoryResolver.allAttributeRepositories();
        }

    }
}
