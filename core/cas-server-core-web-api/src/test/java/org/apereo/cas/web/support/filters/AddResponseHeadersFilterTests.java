package org.apereo.cas.web.support.filters;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AddResponseHeadersFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class AddResponseHeadersFilterTests {

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val ctx = new MockServletContext();

        val filter = new AddResponseHeadersFilter();
        filter.setHeadersMap(CollectionUtils.wrap("key1", "value1", "key2", "value2"));
        val config = new MockFilterConfig(ctx);
        config.addInitParameter("key3", "value3");
        filter.init(config);
        filter.doFilter(request, response, new MockFilterChain());
        assertTrue(response.containsHeader("key1"));
        assertTrue(response.containsHeader("key2"));
        filter.destroy();
    }
}
