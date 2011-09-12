/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.net.URLEncoder;
import java.util.Map;

/**
 * Encapsulates a Response to send back for a particular service.
 * 
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class Response {

    public static enum ResponseType {
        POST, REDIRECT
    }

    private final ResponseType responseType;

    private final String url;

    private final Map<String, String> attributes;

    protected Response(ResponseType responseType, final String url, final Map<String, String> attributes) {
        this.responseType = responseType;
        this.url = url;
        this.attributes = attributes;
    }

    public static Response getPostResponse(final String url, final Map<String, String> attributes) {
        return new Response(ResponseType.POST, url, attributes);
    }

    public static Response getRedirectResponse(final String url, final Map<String, String> parameters) {
        final StringBuilder builder = new StringBuilder(parameters.size() * 40 + 100);
        boolean isFirst = true;
        final String[] fragmentSplit = url.split("#");
        
        builder.append(fragmentSplit[0]);
        
        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                if (isFirst) {
                    builder.append(url.contains("?") ? "&" : "?");
                    isFirst = false;
                } else {
                    builder.append("&");   
                }
                builder.append(entry.getKey());
                builder.append("=");

                try {
                    builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (final Exception e) {
                    builder.append(entry.getValue());
                }
            }
        }

        if (fragmentSplit.length > 1) {
            builder.append("#");
            builder.append(fragmentSplit[1]);
        }

        return new Response(ResponseType.REDIRECT, builder.toString(), parameters);
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public ResponseType getResponseType() {
        return this.responseType;
    }

    public String getUrl() {
        return this.url;
    }
}
