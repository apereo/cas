package org.apereo.cas.services.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceChainingAttributeFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class RegisteredServiceChainingAttributeFilterTests {

    @Test
    public void verifyOperation() {
        val chain = new RegisteredServiceChainingAttributeFilter();
        chain.getFilters().add(new RegisteredServiceRegexAttributeFilter(RegexUtils.MATCH_NOTHING_PATTERN.pattern()));
        assertTrue(chain.filter(CoreAuthenticationTestUtils.getAttributes()).isEmpty());
    }
}
