package org.apereo.cas.persondir;

import org.apereo.cas.BasePrincipalAttributeRepositoryTests;
import org.apereo.cas.authentication.attribute.AttributeRepositoryQuery;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAttributeRepositoryResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Attributes")
@ExtendWith(CasTestExtension.class)
class DefaultAttributeRepositoryResolverTests {

    @Nested
    @SpringBootTest(classes = {
        CasCoreServicesAutoConfiguration.class,
        BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    class DefaultTests {

        @Autowired
        @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
        private AttributeRepositoryResolver attributeRepositoryResolver;

        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        private ServicesManager servicesManager;

        @Test
        void verifyRepositoriesByServiceAssignment() {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            val releasePolicy = new DenyAllAttributeReleasePolicy();
            releasePolicy.setPrincipalAttributesRepository(new DefaultPrincipalAttributesRepository().setAttributeRepositoryIds(Set.of("stub")));

            registeredService.setAttributeReleasePolicy(releasePolicy);
            servicesManager.save(registeredService);
            
            val query = AttributeRepositoryQuery.builder()
                .activeRepositoryIds(Set.of("stub", "ldap"))
                .authenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("simpleHandler"))
                .principal(RegisteredServiceTestUtils.getPrincipal())
                .service(RegisteredServiceTestUtils.getService(registeredService.getServiceId()))
                .build();
            val results = attributeRepositoryResolver.resolve(query);
            assertEquals(1, results.size());
            assertTrue(results.contains("stub"));
        }

        @Test
        void verifyRepositoriesRequestedByQuery() {
            val query = AttributeRepositoryQuery.builder()
                .activeRepositoryIds(Set.of("stub", "ldap"))
                .authenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("simpleHandler"))
                .principal(RegisteredServiceTestUtils.getPrincipal())
                .service(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()))
                .build();
            val results = attributeRepositoryResolver.resolve(query);
            assertEquals(2, results.size());
            assertTrue(results.containsAll(query.getActiveRepositoryIds()));
        }

        @Test
        void verifyRepositoriesUndefinedInQuery() {
            val query = AttributeRepositoryQuery.builder()
                .authenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("simpleHandler"))
                .principal(RegisteredServiceTestUtils.getPrincipal())
                .service(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()))
                .build();
            val results = attributeRepositoryResolver.resolve(query);
            assertEquals(1, results.size());
            assertTrue(results.contains(PersonAttributeDao.WILDCARD));
        }
    }

    @Nested
    @SpringBootTest(classes = {
        CasCoreServicesAutoConfiguration.class,
        BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class
    }, properties = {
        "cas.person-directory.attribute-repository-selection.handler1=stub,other",
        "cas.person-directory.attribute-repository-selection.handler2=other,ldap"
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    class AuthHandlerTests {

        @Autowired
        @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
        private AttributeRepositoryResolver attributeRepositoryResolver;
        
        @Test
        void verifyRepositoriesRequestedByHandler() {
            var query = AttributeRepositoryQuery.builder()
                .activeRepositoryIds(Set.of("stub", "ldap", "other"))
                .authenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("handler1"))
                .principal(RegisteredServiceTestUtils.getPrincipal())
                .service(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()))
                .build();
            var results = attributeRepositoryResolver.resolve(query);
            assertEquals(2, results.size());
            assertTrue(results.containsAll(List.of("stub", "other")));

            query = query.withAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler("handler2"));
            results = attributeRepositoryResolver.resolve(query);
            assertEquals(2, results.size());
            assertTrue(results.containsAll(List.of("ldap", "other")));
        }
    }
}
