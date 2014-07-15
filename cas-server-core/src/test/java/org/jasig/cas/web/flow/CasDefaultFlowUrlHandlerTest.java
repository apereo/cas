/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class CasDefaultFlowUrlHandlerTest {

    @Test
    public void test_flow_execution_url_contains_incoming_parameters(){
        CasDefaultFlowUrlHandler flowUrlHandler = new CasDefaultFlowUrlHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login");
        request.addParameter("username", "foo");
        request.addParameter("password", "bar");
        request.addParameter("execution", "e1s1");
        request.addParameter("_eventId", "submit");
        request.addParameter("lt", "LT-4-zVaB4ZYG451qx9eY1ozNU2y9LUvufu");
        String url = flowUrlHandler.createFlowExecutionUrl("login", "e1s1", request);

        Assert.assertTrue(url.contains("_eventId=submit"));
        Assert.assertTrue(url.contains("lt=LT-4-zVaB4ZYG451qx9eY1ozNU2y9LUvufu"));
        Assert.assertTrue(url.contains("username=foo"));
        Assert.assertTrue(url.contains("password=bar"));
        Assert.assertTrue(url.contains("execution=e1s1"));
        System.out.println(url);
    }
}
