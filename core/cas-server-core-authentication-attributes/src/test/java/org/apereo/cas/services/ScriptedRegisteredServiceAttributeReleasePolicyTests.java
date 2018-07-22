package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * This is {@link ScriptedRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(JUnit4.class)
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
