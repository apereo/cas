package org.apereo.cas.support.openid.web.support;

import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;


/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdPostUrlHandlerMappingTests extends AbstractOpenIdTests {

    @Autowired
    private OpenIdPostUrlHandlerMapping handlerMapping;
    
    @Test
    public void verifyNoMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");

        assertNull(this.handlerMapping.lookupHandler("/hello", request));
    }

    @Test
    public void verifyImproperMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    @Test
    public void verifyProperMatchWrongMethod() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("GET");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    @Test
    public void verifyProperMatchCorrectMethodNoParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    @Test
    public void verifyProperMatchCorrectMethodWithParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");
        request.setParameter("openid.mode", "check_authentication");


        assertNotNull(this.handlerMapping.lookupHandler("/login", request));
    }
}
