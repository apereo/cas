/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */

package org.jasig.cas.adaptors.cas.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

/**
 * A poor-man's mock servlet request.  Usage of this class should probably be
 * replaced by usage of a real mock objects library.
 * @version $Revision$ $Date$
 */
public class MockServletRequest 
    implements ServletRequest {

    public Object getAttribute(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        
    }

    public int getContentLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getParameterValues(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map getParameterMap() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRemoteHost() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttribute(String arg0, Object arg1) {
        // TODO Auto-generated method stub
        
    }

    public void removeAttribute(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRealPath(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}

