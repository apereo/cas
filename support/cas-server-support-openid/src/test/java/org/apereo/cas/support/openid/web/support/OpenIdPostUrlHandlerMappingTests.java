package org.apereo.cas.support.openid.web.support;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;


/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
public class OpenIdPostUrlHandlerMappingTests extends AbstractOpenIdTests {

    private static final String LOGIN_URL_PATH = "/login";
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

        assertNull(this.handlerMapping.lookupHandler(LOGIN_URL_PATH, request));
    }

    @Test
    public void verifyProperMatchWrongMethod() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(LOGIN_URL_PATH);
        request.setMethod("GET");

        assertNull(this.handlerMapping.lookupHandler(LOGIN_URL_PATH, request));
    }

    @Test
    public void verifyProperMatchCorrectMethodNoParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(LOGIN_URL_PATH);
        request.setMethod("POST");

        assertNull(this.handlerMapping.lookupHandler(LOGIN_URL_PATH, request));
    }

    @Test
    public void verifyProperMatchCorrectMethodWithParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(LOGIN_URL_PATH);
        request.setMethod("POST");
        request.setParameter("openid.mode", "check_authentication");


        assertNotNull(this.handlerMapping.lookupHandler(LOGIN_URL_PATH, request));
    }
}
