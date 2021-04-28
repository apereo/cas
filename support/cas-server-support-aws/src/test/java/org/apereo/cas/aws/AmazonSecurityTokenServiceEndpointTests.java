package org.apereo.cas.aws;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.AmazonCoreConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.rest.config.CasCoreRestConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonSecurityTokenServiceEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("AmazonWebServices")
@EnabledIfPortOpen(port = 4566)
public class AmazonSecurityTokenServiceEndpointTests {
    @TestPropertySource(properties = {
        "cas.amazon-sts.endpoint=http://127.0.0.1:4566",
        "cas.amazon-sts.credential-access-key=test",
        "cas.amazon-sts.credential-secret-key=test",
        "cas.authn.mfa.groovy-script.location=classpath:AmazonStsGroovyMfa.groovy",
        "management.endpoint.awsSts.enabled=true"
    })
    @Import({
        AmazonCoreConfiguration.class,
        CasCoreRestConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class
    })
    public static class BaseAmazonSecurityTokenServiceEndpointTests extends AbstractCasEndpointTests {
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=groupMembership",
        "cas.amazon-sts.principal-attribute-value=^un[A-Z]known.*",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=some-value"
    })
    @SuppressWarnings("ClassCanBeStatic")
    public class WithMissingAuthorizationAttributeValues extends BaseAmazonSecurityTokenServiceEndpointTests {
        @Autowired
        @Qualifier("awsSecurityTokenServiceEndpoint")
        private AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @BeforeEach
        public void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        public void verifyAuthzFails() {
            val request = new MockHttpServletRequest();
            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("casuser"));
            body.put("password", List.of("resusac"));
            val status = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null,
                null, null, null, body, request);
            assertEquals(HttpStatus.UNAUTHORIZED, status.getStatusCode());
        }
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = "cas.amazon-sts.principal-attribute-name=unknown")
    @SuppressWarnings("ClassCanBeStatic")
    public class WithMissingAuthorizationAttributes extends BaseAmazonSecurityTokenServiceEndpointTests {
        @Autowired
        @Qualifier("awsSecurityTokenServiceEndpoint")
        private AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @BeforeEach
        public void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        public void verifyAuthzFails() {
            val request = new MockHttpServletRequest();
            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("casuser"));
            body.put("password", List.of("resusac"));
            val status = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null, null,
                null, null, body, request);
            assertEquals(HttpStatus.UNAUTHORIZED, status.getStatusCode());
        }
    }

    @Nested
    @Tag("AmazonWebServices")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=",
        "cas.amazon-sts.principal-attribute-value="
    })
    @SuppressWarnings("ClassCanBeStatic")
    public class WithoutAuthorizationAttributes extends BaseAmazonSecurityTokenServiceEndpointTests {
        @Autowired
        @Qualifier("awsSecurityTokenServiceEndpoint")
        private AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @BeforeEach
        public void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        public void verifyOperation() {
            val request = new MockHttpServletRequest();

            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("casuser"));
            body.put("password", List.of("resusac"));

            val credentials = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null, null,
                null, null, body, request);
            assertEquals(HttpStatus.OK, credentials.getStatusCode());
        }

        @Test
        public void verifyContextValidationFails() {
            val request = new MockHttpServletRequest();

            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("test1234"));
            body.put("password", List.of("4321tset"));

            val status = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null,
                null, null, null, body, request);
            assertEquals(HttpStatus.UNAUTHORIZED, status.getStatusCode());
        }

        @Test
        public void verifyNoCredentials() {
            val request = new MockHttpServletRequest();
            val body = new LinkedMultiValueMap<String, String>();
            val status = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null,
                null, null, null, body, request);
            assertEquals(HttpStatus.UNAUTHORIZED, status.getStatusCode());
        }

        @Test
        public void verifyFailsAuthN() {
            val request = new MockHttpServletRequest();

            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("casuser"));
            body.put("password", List.of("bad-password"));

            val status = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null,
                null, null, null, body, request);
            assertEquals(HttpStatus.UNAUTHORIZED, status.getStatusCode());
        }
    }
    
    @Nested
    @Tag("AmazonWebServices")
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=awsroles",
        "cas.amazon-sts.principal-attribute-value=.+",
        "cas.amazon-sts.rbac-enabled=true",
        "cas.authn.attribute-repository.stub.attributes.awsroles=arn:aws:iam::223873472255:role/adminuser-iam-role"
    })
    public class WithRoleRequest extends BaseAmazonSecurityTokenServiceEndpointTests {
        @Autowired
        @Qualifier("awsSecurityTokenServiceEndpoint")
        private AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @BeforeEach
        public void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        public void verifyOperation() {
            val request = new MockHttpServletRequest();
            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("casuser"));
            body.put("password", List.of("resusac"));
            val credentials = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null,
                null, null, null, body, request);
            assertEquals(HttpStatus.OK, credentials.getStatusCode());
        }
    }

    @Nested
    @Tag("AmazonWebServices")
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.amazon-sts.principal-attribute-name=awsroles",
        "cas.amazon-sts.principal-attribute-value=arn.+",
        "cas.amazon-sts.rbac-enabled=true",
        "cas.authn.attribute-repository.stub.attributes.awsroles=arn:aws:iam::223873472255:role/adminuser-iam-role,arn:aws:iam::123873472251:role/superuser-iam-role"
    })
    public class WithMultipleRolesRequest extends BaseAmazonSecurityTokenServiceEndpointTests {
        @Autowired
        @Qualifier("awsSecurityTokenServiceEndpoint")
        private AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @BeforeEach
        public void beforeEach() {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        @Test
        public void verifyOperation() {
            val request = new MockHttpServletRequest();
            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("casuser"));
            body.put("password", List.of("resusac"));
            val credentials = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null,
                null, null, null, body, request);
            assertEquals(HttpStatus.UNAUTHORIZED, credentials.getStatusCode());
        }

        @Test
        public void verifySpecificUnknownRoleOperation() {
            val request = new MockHttpServletRequest();
            val body = new LinkedMultiValueMap<String, String>();
            body.put("username", List.of("casuser"));
            body.put("password", List.of("resusac"));
            val credentials = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", null,
                null, null, "this-is-unknown-role", body, request);
            assertEquals(HttpStatus.UNAUTHORIZED, credentials.getStatusCode());
        }
    }
    
}
