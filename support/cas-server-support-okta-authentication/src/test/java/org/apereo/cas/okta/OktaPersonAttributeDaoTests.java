package org.apereo.cas.okta;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.config.CasOktaAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.beans.BeanContainer;
import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserProfile;
import com.okta.sdk.resource.user.UserStatus;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.Date;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OktaPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
class OktaPersonAttributeDaoTests {

    @TestConfiguration(value = "OktaClientMockConfiguration", proxyBeanMethods = false)
    static class OktaClientMockTestConfiguration {
        @Bean
        public Client oktaPersonDirectoryClient() {
            val profile = mock(UserProfile.class);
            when(profile.getEmail()).thenReturn("cas@example.org");
            when(profile.getEmployeeNumber()).thenReturn("23947236572");

            val user = mock(User.class);
            when(user.getId()).thenReturn("casuser");
            when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
            when(user.getProfile()).thenReturn(profile);
            when(user.getLastUpdated()).thenReturn(new Date());
            when(user.getLastLogin()).thenReturn(new Date());
            val client = mock(Client.class);
            when(client.getUser(anyString())).thenReturn(user);
            return client;
        }
    }

    @SpringBootTest(classes = {
        CasOktaAuthenticationAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        BaseOktaTests.SharedTestConfiguration.class
    }, properties = {
        "cas.authn.attribute-repository.okta.organization-url=https://dev-668371.oktapreview.com",
        "cas.authn.attribute-repository.okta.api-token=0030j4HfPHEIQG39pl0nNacnx2bqqZMqDq6Hk5wfNa"
    })
    @Nested
    class OktaPersonDirectoryConfigurationTests {
        @Autowired
        @Qualifier("oktaPersonAttributeDaos")
        private BeanContainer<PersonAttributeDao> oktaPersonAttributeDaos;

        @Autowired
        @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
        private PersonAttributeDao attributeRepository;

        @Test
        void verifyOperation() {
            assertEquals(1, oktaPersonAttributeDaos.size());
            assertNull(attributeRepository.getPerson("casuser"));

            val dao = (OktaPersonAttributeDao) oktaPersonAttributeDaos.toList().getFirst();
            assertTrue(dao.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose()).isEmpty());
            assertTrue(dao.getAvailableQueryAttributes(PersonAttributeDaoFilter.alwaysChoose()).isEmpty());
            assertNotNull(dao.getOktaClient());
        }
    }

    @SpringBootTest(classes = {
        OktaPersonAttributeDaoTests.OktaClientMockTestConfiguration.class,
        CasOktaAuthenticationAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        BaseOktaTests.SharedTestConfiguration.class
    }, properties = {
        "cas.authn.attribute-repository.okta.organization-url=https://dev-668371.oktapreview.com",
        "cas.authn.attribute-repository.okta.api-token=0030j4HfPHEIQG39pl0nNacnx2bqqZMqDq6Hk5wfNa"
    })
    @Nested
    class OktaPersonDirectoryMockTests {
        @Autowired
        @Qualifier("oktaPersonAttributeDaos")
        private BeanContainer<PersonAttributeDao> oktaPersonAttributeDaos;

        @Autowired
        @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
        private PersonAttributeDao attributeRepository;

        @Test
        void verifyOperation() {
            assertFalse(oktaPersonAttributeDaos.toList().isEmpty());
            assertNotNull(attributeRepository.getPerson("casuser"));
            assertFalse(attributeRepository.getPeople(Map.of("username", "casuser"),
                PersonAttributeDaoFilter.alwaysChoose()).isEmpty());
        }
    }

}
