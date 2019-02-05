package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScriptedRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ScriptedRegisteredServiceAttributeReleasePolicyTests {

    @Test
    public void verifyInlineScript() {
        val p = new ScriptedRegisteredServiceAttributeReleasePolicy();
        p.setScriptFile("groovy { return attributes }");
        val principal = CoreAttributesTestUtils.getPrincipal("cas",
            Collections.singletonMap("attribute", "value"));
        val attrs = p.getAttributes(principal,
            CoreAttributesTestUtils.getService(),
            CoreAttributesTestUtils.getRegisteredService());
        assertEquals(attrs.size(), principal.getAttributes().size());
    }
}
