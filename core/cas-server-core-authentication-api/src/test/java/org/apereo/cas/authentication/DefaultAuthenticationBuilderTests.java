package org.apereo.cas.authentication;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Authentication")
public class DefaultAuthenticationBuilderTests {
    @Test
    public void verifyOperation() {
        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder.addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, meta));
        builder.setCredentials(List.of(meta));

        val result = new DefaultAuthenticationHandlerExecutionResult(new SimpleTestUsernamePasswordAuthenticationHandler(), meta);
        builder.addSuccess("Success", result);
        builder.setFailures(Map.of("Failure1", new FailedLoginException()));
        builder.addFailure("Success", new FailedLoginException());
        assertFalse(builder.hasAttribute("invalid"));
        assertNotNull(builder.build());
    }

    @Test
    public void verifyUpdateOperation() {
        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder.addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, meta));
        builder.setCredentials(List.of(meta));
        val authn = builder.build();

        val builder2 = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder2.setAuthenticationDate(ZonedDateTime.now(Clock.systemUTC()).plusDays(10));
        builder2.addAttribute("authn2", "value2");
        builder2.addWarning(new DefaultMessageDescriptor("code"));
        val authn2 = builder2.build();

        authn.updateAll(authn2);
        assertTrue(authn.getAttributes().containsKey("authn2"));
    }
}
