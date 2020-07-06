package org.apereo.cas.configuration.model.support.syncope;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopeAuthenticationPropertiesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("CasConfiguration")
public class SyncopeAuthenticationPropertiesTests {

    @Test
    public void verifyUndefined() {
        val props = new SyncopeAuthenticationProperties();
        assertTrue(props.isUndefined());
        assertFalse(props.isDefined());
    }

    @Test
    public void verifyDefined() {
        val props = new SyncopeAuthenticationProperties();
        props.setUrl("https://syncope.apache.org");
        props.setDomain("Master");
        assertFalse(props.isUndefined());
        assertTrue(props.isDefined());
    }
}
