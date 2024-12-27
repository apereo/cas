package org.apereo.cas.authentication;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
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
class DefaultAuthenticationBuilderTests {
    private static MutableCredential getCredential() {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("casuser");
        credential.assignPassword("P@$$w0rd");
        return credential;
    }

    @Test
    void verifyMergeCredentialMetadata() {
        val credential1 = getCredential();
        credential1.setCredentialMetadata(new BasicCredentialMetadata(credential1, Map.of("P1", "V1")));
        val credential2 = getCredential();
        credential2.setCredentialMetadata(new BasicCredentialMetadata(credential2, Map.of("P2", "V1")));

        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder.addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, credential1));
        builder.setCredentials(List.of(credential1, credential2));

        val result = new DefaultAuthenticationHandlerExecutionResult(new SimpleTestUsernamePasswordAuthenticationHandler(), credential2);
        builder.addSuccess("Success", result);
        builder.setFailures(Map.of("Failure1", new FailedLoginException()));
        builder.addFailure("Success", new FailedLoginException());
        assertFalse(builder.hasAttribute("invalid"));
        val authentication = builder.build();
        assertNotNull(authentication);
        assertEquals(1, authentication.getCredentials().size());
        val credentialMetaData = authentication.getCredentials().getFirst().getCredentialMetadata();
        assertEquals(2, credentialMetaData.getProperties().size());
        assertTrue(credentialMetaData.getProperties().containsKey("P1"));
        assertTrue(credentialMetaData.getProperties().containsKey("P2"));
    }

    @Test
    void verifyOperation() {
        val credential = getCredential();
        credential.setCredentialMetadata(new BasicCredentialMetadata(credential));

        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder.addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, credential));
        builder.setCredentials(List.of(getCredential()));

        val result = new DefaultAuthenticationHandlerExecutionResult(new SimpleTestUsernamePasswordAuthenticationHandler(), credential);
        builder.addSuccess("Success", result);
        builder.setFailures(Map.of("Failure1", new FailedLoginException()));
        builder.addFailure("Success", new FailedLoginException());
        assertFalse(builder.hasAttribute("invalid"));
        assertNotNull(builder.build());
    }

    @Test
    void verifyMergeOperation() throws Throwable {
        val builder1 = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder1.mergeAttribute("key", 12345);
        builder1.mergeAttribute("key", CollectionUtils.wrapList(54321, 998877));
        val authn1 = builder1.build();

        val builder2 = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder2.mergeAttribute("key", 112233);
        builder2.mergeAttribute("key", CollectionUtils.wrapList(443322));
        val authn2 = builder2.build();

        val strategy = new DefaultPrincipalElectionStrategy();
        val resultBuilder = new DefaultAuthenticationResultBuilderFactory(strategy).newBuilder();
        strategy.setAttributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED));
        val resultAuthn = resultBuilder.collect(authn1).collect(authn2).build();
        assertNotNull(resultAuthn);
        assertEquals(5, resultAuthn.getAuthentication().getAttributes().get("key").size());
    }

    @Test
    void verifyUpdateOperation() {
        val credential = getCredential();
        credential.setCredentialMetadata(new BasicCredentialMetadata(credential));
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder.addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, credential));
        builder.setCredentials(List.of(credential));
        val authn = builder.build();

        val builder2 = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal());
        builder2.setAuthenticationDate(ZonedDateTime.now(Clock.systemUTC()).plusDays(10));
        builder2.addAttribute("authn2", "value2");
        builder2.addWarning(new DefaultMessageDescriptor("code"));
        val authn2 = builder2.build();

        authn.replaceAttributes(authn2);
        assertTrue(authn.containsAttribute("authn2"));
        assertTrue(authn.containsAttribute("authn2"));
    }

    @Test
    void verifyMergeAttributes() {
        val authn = DefaultAuthenticationBuilder.newInstance(CoreAuthenticationTestUtils.getAuthentication(Map.of("cn", List.of("cn1"))))
            .mergeAttributes(Map.of("cn", List.of("cn2")))
            .build();
        assertTrue(authn.containsAttribute("cn"));
        assertEquals(List.of("cn1", "cn2"), authn.getAttributes().get("cn"));
    }
}
