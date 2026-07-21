package org.apereo.cas.aws;

import module java.base;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasAmazonCoreAutoConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreRestAutoConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link AmazonSecurityTokenServiceEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("AmazonWebServices")
@EnabledIfListeningOnPort(port = 4566)
class AmazonSecurityTokenServiceEndpointTests {
    @TestPropertySource(properties = {
        "cas.amazon-sts.endpoint=http://127.0.0.1:4566",
        "cas.amazon-sts.region=us-east-1",
        "cas.amazon-sts.credential-access-key=test",
        "cas.amazon-sts.credential-secret-key=test",
        "cas.authn.mfa.groovy-script.location=classpath:AmazonStsGroovyMfa.groovy",
        "management.endpoint.awsSts.access=UNRESTRICTED"
    })
    @Import(CasAuthenticationEventExecutionPlanTestConfiguration.class)
    @ImportAutoConfiguration({
        CasAmazonCoreAutoConfiguration.class,
        CasCoreRestAutoConfiguration.class
    })
    static class BaseAmazonSecurityTokenServiceEndpointTests extends AbstractCasEndpointTests {
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=groupMembership",
        "cas.amazon-sts.principal-attribute-value=^un[A-Z]known.*",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=some-value"
    })
    class WithMissingAuthorizationAttributeValues extends BaseAmazonSecurityTokenServiceEndpointTests {
        @BeforeEach
        void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        void verifyAuthzFails() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "casuser")
                    .param("password", "resusac"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = "cas.amazon-sts.principal-attribute-name=unknown")
    class WithMissingAuthorizationAttributes extends BaseAmazonSecurityTokenServiceEndpointTests {
        @BeforeEach
        void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        void verifyAuthzFails() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "casuser")
                    .param("password", "resusac"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=",
        "cas.amazon-sts.principal-attribute-value="
    })
    class WithoutAuthorizationAttributes extends BaseAmazonSecurityTokenServiceEndpointTests {
        @BeforeEach
        void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        void verifyOperation() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "casuser")
                    .param("password", "resusac"))
                .andExpect(status().isOk());
        }

        @Test
        void verifyContextValidationFails() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "test1234")
                    .param("password", "4321tset"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void verifyNoCredentials() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void verifyFailsAuthN() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "casuser")
                    .param("password", "bad-password"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=awsroles",
        "cas.amazon-sts.principal-attribute-value=.+",
        "cas.amazon-sts.rbac-enabled=true",
        "cas.authn.attribute-repository.stub.attributes.awsroles=arn:aws:iam::223873472255:role/adminuser-iam-role"
    })
    class WithRoleRequest extends BaseAmazonSecurityTokenServiceEndpointTests {
        @BeforeEach
        void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        void verifyOperation() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "casuser")
                    .param("password", "resusac"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=awsroles",
        "cas.amazon-sts.principal-attribute-value=arn.+",
        "cas.amazon-sts.rbac-enabled=true",
        "cas.authn.attribute-repository.stub.attributes.awsroles=arn:aws:iam::223873472255:role/adminuser-iam-role,arn:aws:iam::123873472251:role/superuser-iam-role"
    })
    class WithMultipleRolesRequest extends BaseAmazonSecurityTokenServiceEndpointTests {
        @BeforeEach
        void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        void verifyOperation() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "casuser")
                    .param("password", "resusac"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void verifySpecificUnknownRoleOperation() throws Throwable {
            mockMvc.perform(post("/actuator/awsSts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "casuser")
                    .param("password", "resusac")
                    .param("roleArn", "this-is-unknown-role"))
                .andExpect(status().isUnauthorized());
        }
    }

}
