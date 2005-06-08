/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.mock.MockAuthentication;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * NOTE: This file checks for one less \n than needed as the server tends to add the extra one itself.
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 *
 */
public class Cas10ResponseViewTests extends TestCase {

    private final Cas10ResponseView view = new Cas10ResponseView();

    String response;

    public void testSuccessView() throws Exception {
        this.view.setSuccessResponse(true);
        Map model = new HashMap();
        Authentication authentication = new MockAuthentication(
            new SimplePrincipal("test"));
        List list = new ArrayList();
        list.add(authentication);
        Assertion assertion = new ImmutableAssertionImpl(list,
            new SimpleService("TestService"), true);
        model.put(WebConstants.ASSERTION, assertion);

        this.view.render(model, new MockHttpServletRequest(),
            new MockWriterHttpMockHttpServletResponse());
        assertEquals("yes\ntest", this.response);

    }

    public void testFailureView() throws Exception {
        this.view.setSuccessResponse(false);
        Map model = new HashMap();
        Authentication authentication = new MockAuthentication(
            new SimplePrincipal("test"));
        List list = new ArrayList();
        list.add(authentication);
        Assertion assertion = new ImmutableAssertionImpl(list,
            new SimpleService("TestService"), true);
        model.put(WebConstants.ASSERTION, assertion);

        this.view.render(model, new MockHttpServletRequest(),
            new MockWriterHttpMockHttpServletResponse());
        assertEquals("no\n", this.response);
    }

    protected class MockWriterHttpMockHttpServletResponse implements
        HttpServletResponse {

        public void addCookie(Cookie arg0) {
            // nothing to do
        }

        public void addDateHeader(String arg0, long arg1) {
            // nothing to do
        }

        public void addHeader(String arg0, String arg1) {
            // nothing to do
        }

        public void addIntHeader(String arg0, int arg1) {
            // nothing to do
        }

        public boolean containsHeader(String arg0) {
            return false;
        }

        public String encodeRedirectUrl(String arg0) {
            return null;
        }

        public String encodeRedirectURL(String arg0) {
            return null;
        }

        public String encodeUrl(String arg0) {
            return null;
        }

        public String encodeURL(String arg0) {
            return null;
        }

        public void sendError(int arg0, String arg1) throws IOException {
            // nothing to do            
        }

        public void sendError(int arg0) throws IOException {
            // nothing to do
        }

        public void sendRedirect(String arg0) throws IOException {
            // nothing to do
        }

        public void setDateHeader(String arg0, long arg1) {
            // nothing to do
        }

        public void setHeader(String arg0, String arg1) {
            // nothing to do

        }

        public void setIntHeader(String arg0, int arg1) {
            // nothing to do

        }

        public void setStatus(int arg0, String arg1) {
            // nothing to do

        }

        public void setStatus(int arg0) {
            // nothing to do

        }

        public void flushBuffer() throws IOException {
            // nothing to do

        }

        public int getBufferSize() {
            return 0;
        }

        public String getCharacterEncoding() {
            return null;
        }

        public Locale getLocale() {
            return null;
        }

        public ServletOutputStream getOutputStream() throws IOException {
            // nothing to do
            return null;
        }

        public PrintWriter getWriter() throws IOException {
            return new MockPrintWriter(new ByteArrayOutputStream());
        }

        public boolean isCommitted() {
            // nothing to do
            return false;
        }

        public void reset() {
            // nothing to do

        }

        public void resetBuffer() {
            // nothing to do

        }

        public void setBufferSize(int arg0) {
            // nothing to do

        }

        public void setContentLength(int arg0) {
            // nothing to do

        }

        public void setContentType(String arg0) {
            // nothing to do

        }

        public void setLocale(Locale arg0) {
            // nothing to do
        }

    }

    protected class MockPrintWriter extends PrintWriter {

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
