package org.jasig.cas.authentication.principal;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4.4
 */
public class ResponseTests extends TestCase {

     public void testConstructionWithoutFragmentAndNoQueryString() {
        final String url = "http://localhost:8080/foo";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals(url + "?ticket=foobar", response.getUrl());
    }

    public void testConstructionWithoutFragmentButHasQueryString() {
        final String url = "http://localhost:8080/foo?test=boo";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals(url + "&ticket=foobar", response.getUrl());
    }

    public void testConstructionWithFragmentAndQueryString() {
        final String url = "http://localhost:8080/foo?test=boo#hello";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?test=boo&ticket=foobar#hello", response.getUrl());
    }

    public void testConstructionWithFragmentAndNoQueryString() {
        final String url = "http://localhost:8080/foo#hello";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?ticket=foobar#hello", response.getUrl());

    }
}
