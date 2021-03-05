package org.apereo.cas.services.support;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceReverseMappedRegexAttributeFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
public class RegisteredServiceReverseMappedRegexAttributeFilterTests {
    @Test
    public void verifyOperation() {
        val filter = new RegisteredServiceReverseMappedRegexAttributeFilter();
        filter.setPatterns(Map.of("username", "^cas_user$"));
        val attrs = CollectionUtils.<String, List<Object>>wrap("username", List.of("cas-user"));
        assertFalse(filter.filter(attrs).isEmpty());
    }

    @Test
    public void verifyMatch() {
        val filter = new RegisteredServiceReverseMappedRegexAttributeFilter();
        filter.setCompleteMatch(true);
        filter.setPatterns(Map.of("username", "^cas_user$"));
        val attrs = CollectionUtils.<String, List<Object>>wrap("username", List.of("cas-user"));
        assertFalse(filter.filter(attrs).isEmpty());
    }
}
