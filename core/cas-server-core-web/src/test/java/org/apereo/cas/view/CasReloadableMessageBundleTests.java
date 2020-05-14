package org.apereo.cas.view;

import org.apereo.cas.web.view.CasReloadableMessageBundle;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasReloadableMessageBundleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CasReloadableMessageBundleTests {

    @Test
    public void verifyDefaultMessage() {
        val bundle = new CasReloadableMessageBundle();
        assertNull(bundle.getMessage("cas.message",
            ArrayUtils.EMPTY_STRING_ARRAY, null, Locale.ENGLISH));
    }

    @Test
    public void verifyMessage() {
        val bundle = new CasReloadableMessageBundle();
        bundle.setBasenames("messages");
        assertNull(bundle.getMessage("cas.message",
            ArrayUtils.EMPTY_STRING_ARRAY, null, Locale.ITALIAN));
    }
}
