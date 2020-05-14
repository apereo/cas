package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
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
}
