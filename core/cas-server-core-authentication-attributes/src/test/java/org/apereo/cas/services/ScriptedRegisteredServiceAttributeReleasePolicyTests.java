package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScriptedRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 * @deprecated Since 6.2
 */
@Tag("Groovy")
@Deprecated(since = "6.2.0")
public class ScriptedRegisteredServiceAttributeReleasePolicyTests {

    @Test
    public void verifyInlineScript() {
        val p = new ScriptedRegisteredServiceAttributeReleasePolicy();
        p.setScriptFile("groovy { return attributes }");
        val principal = CoreAttributesTestUtils.getPrincipal("cas",
            Collections.singletonMap("attribute", List.of("value")));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val attrs = p.getAttributes(releasePolicyContext);
        assertEquals(attrs.size(), principal.getAttributes().size());
    }
}
