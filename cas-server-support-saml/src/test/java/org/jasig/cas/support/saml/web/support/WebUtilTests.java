package org.jasig.cas.support.saml.web.support;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.DefaultArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
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

        final Service service = WebUtils.getService(Arrays.asList(argumentExtractors), request);

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
