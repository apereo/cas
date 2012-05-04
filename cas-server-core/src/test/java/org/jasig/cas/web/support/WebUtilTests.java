/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import java.util.Arrays;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class WebUtilTests extends TestCase {

    public void testFindService() {
        final SamlArgumentExtractor openIdArgumentExtractor = new SamlArgumentExtractor();
        final CasArgumentExtractor casArgumentExtractor = new CasArgumentExtractor();
        final ArgumentExtractor[] argumentExtractors = new ArgumentExtractor[] {
            openIdArgumentExtractor, casArgumentExtractor};
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Arrays
            .asList(argumentExtractors), request);

        assertEquals("test", service.getId());
    }
    
    public void testFoundNoService() {
        final SamlArgumentExtractor openIdArgumentExtractor = new SamlArgumentExtractor();
        final ArgumentExtractor[] argumentExtractors = new ArgumentExtractor[] {
            openIdArgumentExtractor};
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Arrays
            .asList(argumentExtractors), request);

        assertNull(service);
    }
    /*
     * public void testStripJsessionWithoutQueryStringParameters() {
     * assertEquals("test", WebUtils.stripJsessionFromUrl("test"));
     * assertEquals("http://www.cnn.com",
     * WebUtils.stripJsessionFromUrl("http://www.cnn.com;jsession=fsfsadfsdfsafsd")); }
     * public void testStripJsessionWithQueryStringParameters() {
     * assertEquals("test", WebUtils.stripJsessionFromUrl("test"));
     * assertEquals("http://localhost:8080/WebModule2/jsplevel0.jsp?action=test",
     * WebUtils.stripJsessionFromUrl("http://localhost:8080/WebModule2/jsplevel0.jsp;jsessionid=CC80B7CC9D62689578A99DB90B187A62?action=test")); }
     * public void testStripJsessionWithQueryStringParametersBeforeJsession() {
     * assertEquals("test", WebUtils.stripJsessionFromUrl("test"));
     * assertEquals("http://localhost:8080/WebModule2/jsplevel0.jsp?action=test",
     * WebUtils.stripJsessionFromUrl("http://localhost:8080/WebModule2/jsplevel0.jsp?action=test;jsessionid=CC80B7CC9D62689578A99DB90B187A62")); }
     */
}
