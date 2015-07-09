/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication.principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates a Response to send back for a particular service.
 *
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */
public final class DefaultResponse implements Response {
    /** Log instance. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultResponse.class);

    /** Pattern to detect unprintable ASCII characters. */
    private static final Pattern NON_PRINTABLE = Pattern.compile("[\\x00-\\x1F\\x7F]+");
    private static final int CONST_REDIRECT_RESPONSE_MULTIPLIER = 40;
    private static final int CONST_REDIRECT_RESPONSE_BUFFER = 100;
    private static final long serialVersionUID = -8251042088720603062L;

    private final ResponseType responseType;

    private final String url;

    private final Map<String, String> attributes;

    /**
     * Instantiates a new response.
     *
     * @param responseType the response type
     * @param url the url
     * @param attributes the attributes
     */
    protected DefaultResponse(final ResponseType responseType, final String url, final Map<String, String> attributes) {
        this.responseType = responseType;
        this.url = url;
        this.attributes = attributes;
    }

    /**
     * Gets the post response.
     *
     * @param url the url
     * @param attributes the attributes
     * @return the post response
     */
    public static Response getPostResponse(final String url, final Map<String, String> attributes) {
        return new DefaultResponse(ResponseType.POST, url, attributes);
    }

    /**
     * Gets the redirect response.
     *
     * @param url the url
     * @param parameters the parameters
     * @return the redirect response
     */
    public static Response getRedirectResponse(final String url, final Map<String, String> parameters) {
        final StringBuilder builder = new StringBuilder(parameters.size()
                * CONST_REDIRECT_RESPONSE_MULTIPLIER + CONST_REDIRECT_RESPONSE_BUFFER);
        boolean isFirst = true;
        final String[] fragmentSplit = sanitizeUrl(url).split("#");

        builder.append(fragmentSplit[0]);

        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                if (isFirst) {
                    builder.append(url.contains("?") ? "&" : "?");
                    isFirst = false;
                } else {
                    builder.append('&');
                }
                builder.append(entry.getKey());
                builder.append('=');

                try {
                    builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (final Exception e) {
                    builder.append(entry.getValue());
                }
            }
        }

        if (fragmentSplit.length > 1) {
            builder.append('#');
            builder.append(fragmentSplit[1]);
        }

        return new DefaultResponse(ResponseType.REDIRECT, builder.toString(), parameters);
    }

    @Override
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    @Override
    public Response.ResponseType getResponseType() {
        return this.responseType;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * Sanitize a URL provided by a relying party by normalizing non-printable
     * ASCII character sequences into spaces.  This functionality protects
     * against CRLF attacks and other similar attacks using invisible characters
     * that could be abused to trick user agents.
     *
     * @param  url  URL to sanitize.
     *
     * @return  Sanitized URL string.
     */
    private static String sanitizeUrl(final String url) {
        final Matcher m = NON_PRINTABLE.matcher(url);
        final StringBuffer sb = new StringBuffer(url.length());
        boolean hasNonPrintable = false;
        while (m.find()) {
            m.appendReplacement(sb, " ");
            hasNonPrintable = true;
        }
        m.appendTail(sb);
        if (hasNonPrintable) {
            LOGGER.warn("The following redirect URL has been sanitized and may be sign of attack:\n{}", url);
        }
        return sb.toString();
    }
}
