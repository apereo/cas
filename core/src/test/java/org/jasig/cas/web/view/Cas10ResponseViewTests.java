/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public class Cas10ResponseViewTests extends TestCase {

    private final Cas10ResponseView view = new Cas10ResponseView();

    private Map model;

    String response;

    protected void setUp() throws Exception {
        this.model = new HashMap();
        List list = new ArrayList();
        list.add(new ImmutableAuthentication(new SimplePrincipal("test")));
        this.model.put(WebConstants.ASSERTION, new ImmutableAssertionImpl(list,
            new SimpleService("TestService"), true));
    }

    public void testSuccessView() throws Exception {
        this.view.setSuccessResponse(true);
        this.view.render(this.model, new MockHttpServletRequest(),
            new MockWriterHttpMockHttpServletResponse());
        assertEquals("yes\ntest\n", this.response);
    }

    public void testFailureView() throws Exception {
        this.view.setSuccessResponse(false);
        this.view.render(this.model, new MockHttpServletRequest(),
            new MockWriterHttpMockHttpServletResponse());
        assertEquals("no\n\n", this.response);
    }

    private class MockWriterHttpMockHttpServletResponse extends
        MockHttpServletResponse {

        public PrintWriter getWriter() {
            try {
                return new MockPrintWriter(new ByteArrayOutputStream());
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }

    private class MockPrintWriter extends PrintWriter {

        public MockPrintWriter(OutputStream out, boolean autoFlush) {
            super(out, autoFlush);
        }

        public MockPrintWriter(OutputStream out) {
            super(out);
        }

        public MockPrintWriter(Writer out, boolean autoFlush) {
            super(out, autoFlush);
        }

        public MockPrintWriter(Writer out) {
            super(out);
        }

        public void print(String s) {
            Cas10ResponseViewTests.this.response = s;
        }
    }
}
