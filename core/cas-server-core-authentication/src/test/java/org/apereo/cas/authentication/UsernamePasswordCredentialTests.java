package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.mapping.MappingResults;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.validation.DefaultValidationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = "cas.authn.policy.source-selection-enabled=true")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
class UsernamePasswordCredentialTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val input = new UsernamePasswordCredential("casuser", "Mellon".toCharArray(), StringUtils.EMPTY, Map.of());
        assertTrue(input.isValid());
        assertSame(UsernamePasswordCredential.class, input.getClass());

        val context = MockRequestContext.create(applicationContext).withDefaultMessageContext();

        val validationContext = new DefaultValidationContext(context, "submit", mock(MappingResults.class));
        input.validate(validationContext);
        assertTrue(context.getMessageContext().hasErrorMessages());

    }

    @Test
    void verifyInvalidEvent() throws Throwable {
        val input = new UsernamePasswordCredential(null, "Mellon".toCharArray(), StringUtils.EMPTY, Map.of());

        val context = MockRequestContext.create(applicationContext);

        val validationContext = new DefaultValidationContext(context, "whatever", mock(MappingResults.class));
        input.validate(validationContext);
        assertFalse(context.getMessageContext().hasErrorMessages());
    }

    @Test
    void verifySetGetUsername() {
        val credential = new UsernamePasswordCredential();
        val userName = "test";
        credential.setUsername(userName);
        assertEquals(userName, credential.getUsername());
    }

    @Test
    void verifySetGetPassword() {
        val credential = new UsernamePasswordCredential();
        val password = "test";

        credential.assignPassword(password);
        assertEquals(password, credential.toPassword());
    }

    @Test
    void verifyEquals() {
        val c1 = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword();
        assertNotEquals(null, c1);
        val c2 = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val c3 = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", UUID.randomUUID().toString());
        assertEquals(c3, c2);
    }
}
