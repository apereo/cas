package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.BaseAuthenticationTests;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.services.ChainingServicesManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JaasAuthenticationHandlersConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    JaasAuthenticationHandlersConfigurationTests.JaasAuthenticationTestConfiguration.class,
    BaseAuthenticationTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.accept.users=casuser::Mellon,casuser2::Mellon",
    "cas.authn.jaas[0].realm=CAS",
    "cas.authn.jaas[0].password-policy.enabled=true",
    "cas.authn.jaas[0].password-policy.account-state-handling-enabled=true",
    "cas.authn.jaas[0].login-config-type=JavaLoginConfig"
})
class JaasAuthenticationHandlersConfigurationTests {
    @Autowired
    @Qualifier("jaasAuthenticationHandlers")
    private BeanContainer<AuthenticationHandler> jaasAuthenticationHandlers;

    @Autowired
    @Qualifier("jaasPersonDirectoryPrincipalResolvers")
    private BeanContainer<PrincipalResolver> jaasPersonDirectoryPrincipalResolvers;

    @Test
    void verifyOperation() {
        assertFalse(jaasAuthenticationHandlers.isEmpty());
        assertFalse(jaasPersonDirectoryPrincipalResolvers.isEmpty());
    }

    @BeforeEach
    void initialize() throws Exception {
        val resource = new ClassPathResource("jaas-system.conf");
        val fileName = new File(FileUtils.getTempDirectory(), "jaas-authn.conf");
        try (val writer = Files.newBufferedWriter(fileName.toPath(), StandardCharsets.UTF_8)) {
            IOUtils.copy(resource.getInputStream(), writer, Charset.defaultCharset());
            writer.flush();
        }
        if (fileName.exists()) {
            System.setProperty("java.security.auth.login.config", '=' + fileName.getCanonicalPath());
        }
    }

    @TestConfiguration(value = "JaasAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class JaasAuthenticationTestConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
        public PersonAttributeDao attributeRepository() {
            return new StubPersonAttributeDao();
        }

        @Bean
        @ConditionalOnMissingBean(name = AuthenticationServiceSelectionPlan.BEAN_NAME)
        public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan() {
            return new DefaultAuthenticationServiceSelectionPlan();
        }

        @Bean
        @ConditionalOnMissingBean(name = ServicesManager.BEAN_NAME)
        public ChainingServicesManager servicesManager() {
            return mock(ChainingServicesManager.class);
        }

        @Bean
        @ConditionalOnMissingBean(name = AttributeRepositoryResolver.BEAN_NAME)
        public AttributeRepositoryResolver attributeRepositoryResolver() {
            return query -> Set.of(PersonAttributeDao.WILDCARD);
        }
    }
}
