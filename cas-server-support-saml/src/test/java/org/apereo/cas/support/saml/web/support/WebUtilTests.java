package org.apereo.cas.support.saml.web.support;

import static org.junit.Assert.*;

import java.util.Arrays;

import com.google.common.collect.Lists;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class WebUtilTests {

    @Test
    public void verifyFindService() {

        final DefaultArgumentExtractor casArgumentExtractor = new DefaultArgumentExtractor(
                new WebApplicationServiceFactory()
        );
        final ArgumentExtractor[] argumentExtractors = new ArgumentExtractor[] {
                casArgumentExtractor};
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Lists.newArrayList(argumentExtractors), request);

        assertNotNull(service);
        assertEquals("test", service.getId());
    }

    @Test
    public void verifyFoundNoService() {
        final DefaultArgumentExtractor casArgumentExtractor = new DefaultArgumentExtractor(
                new SamlServiceFactory()
        );

        final ArgumentExtractor[] argumentExtractors = new ArgumentExtractor[] {
                casArgumentExtractor};
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Arrays
                .asList(argumentExtractors), request);

        assertNull(service);
    }
}
