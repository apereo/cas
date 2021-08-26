package org.apereo.cas.authentication;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;

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
    public void verifyMergeOperation() {
        val builder1 = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder1.mergeAttribute("key", 12345);
        builder1.mergeAttribute("key", CollectionUtils.wrapList(54321, 998877));
        val authn1 = builder1.build();

        val builder2 = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder2.mergeAttribute("key", 112233);
        builder2.mergeAttribute("key", CollectionUtils.wrapList(443322));
        val authn2 = builder2.build();

        val resultBuilder = new DefaultAuthenticationResultBuilderFactory().newBuilder();
        val strategy = new DefaultPrincipalElectionStrategy();
        strategy.setAttributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED));
        val resultAuthn = resultBuilder.collect(authn1).collect(authn2).build(strategy);
        assertNotNull(resultAuthn);
        assertEquals(5, resultAuthn.getAuthentication().getAttributes().get("key").size());
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
