package org.apereo.cas.support.saml.web.support;

import com.google.common.collect.Lists;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class WebUtilTests {

    @Test
    public void verifyFindService() {
        final DefaultArgumentExtractor casArgumentExtractor = new DefaultArgumentExtractor(new WebApplicationServiceFactory());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Lists.newArrayList(casArgumentExtractor), request);

        assertNotNull(service);
        assertEquals("test", service.getId());
    }

    @Test
    public void verifyFoundNoService() {
        final DefaultArgumentExtractor casArgumentExtractor = new DefaultArgumentExtractor(new SamlServiceFactory());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Collections.singletonList(casArgumentExtractor), request);

        assertNull(service);
    }
}
