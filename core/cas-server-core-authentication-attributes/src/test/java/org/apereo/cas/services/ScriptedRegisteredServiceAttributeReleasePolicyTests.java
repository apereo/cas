package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link ScriptedRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(JUnit4.class)
@Slf4j
public class ScriptedRegisteredServiceAttributeReleasePolicyTests {

    @Test
    public void verifyInlineScript() {
        final ScriptedRegisteredServiceAttributeReleasePolicy p = new ScriptedRegisteredServiceAttributeReleasePolicy();
        p.setScriptFile("groovy { return attributes }");
        final Principal principal = CoreAttributesTestUtils.getPrincipal("cas",
                Collections.singletonMap("attribute", "value"));
        final Map<String, Object> attrs = p.getAttributes(principal,
            CoreAttributesTestUtils.getService(),
            CoreAttributesTestUtils.getRegisteredService());
        assertEquals(attrs.size(), principal.getAttributes().size());
    }
}
