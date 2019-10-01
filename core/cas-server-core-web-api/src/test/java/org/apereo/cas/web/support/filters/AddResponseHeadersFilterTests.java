package org.apereo.cas.web.support.filters;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AddResponseHeadersFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class AddResponseHeadersFilterTests {

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val ctx = new MockServletContext();

        val filter = new AddResponseHeadersFilter();
        filter.setHeadersMap(Map.of("key1", "value1", "key2", "value2"));
        filter.init(new MockFilterConfig(ctx));
        filter.doFilter(request, response, new MockFilterChain());
        assertTrue(response.containsHeader("key1"));
        assertTrue(response.containsHeader("key2"));
    }
}
