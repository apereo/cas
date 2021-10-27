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
package org.jasig.cas.support.oauth;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * This class has some usefull methods to output data in plain text,
 * handle redirects, add parameter in url or find the right provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthUtils.class);

    /**
     * Instantiates a new OAuth utils.
     */
    private OAuthUtils() {
    }

    /**
     * Write to the output this error text and return a null view.
     *
     * @param response http response
     * @param error error message
     * @param status status code
     * @return a null view
     */
    public static ModelAndView writeTextError(final HttpServletResponse response, final String error, final int status) {
        return OAuthUtils.writeText(response, "error=" + error, status);
    }

    /**
     * Write to the output the text and return a null view.
     *
     * @param response http response
     * @param text output text
     * @param status status code
     * @return a null view
     */
    public static ModelAndView writeText(final HttpServletResponse response, final String text, final int status) {
        try (PrintWriter printWriter = response.getWriter()) {
            response.setStatus(status);
            printWriter.print(text);
        } catch (final IOException e) {
            LOGGER.error("Failed to write to response", e);
        }
        return null;
    }

    /**
     * Return a view which is a redirection to an url with an error parameter.
     *
     * @param url redirect url
     * @param error error message
     * @return A view which is a redirection to an url with an error parameter
     */
    public static ModelAndView redirectToError(final String url, final String error) {
        String useUrl = url;
        if (StringUtils.isBlank(useUrl)) {
            useUrl = "/";
        }
        return OAuthUtils.redirectTo(OAuthUtils.addParameter(useUrl, "error", error));
    }

    /**
     * Return a view which is a redirection to an url.
     *
     * @param url redirect url
     * @return A view which is a redirection to an url
     */
    public static ModelAndView redirectTo(final String url) {
        return new ModelAndView(new RedirectView(url));
    }

    /**
     * Add a parameter with given name and value to an url.
     *
     * @param url url to which parameters will be added
     * @param name name of parameter
     * @param value parameter value
     * @return the url with the parameter
     */
    public static String addParameter(final String url, final String name, final String value) {
        final StringBuilder sb = new StringBuilder();
        sb.append(url);
        if (url.indexOf('?') >= 0) {
            sb.append('&');
        } else {
            sb.append('?');
        }
        sb.append(name);
        sb.append('=');
        if (value != null) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    /**
     * Locate the requested instance of {@link OAuthRegisteredService} by the given clientId.
     * @param servicesManager the service registry DAO instance.
     * @param clientId the client id by which the {@link OAuthRegisteredService} is to be located.
     * @return null, or the located {@link OAuthRegisteredService} instance in the service registry.
     */
    public static OAuthRegisteredService getRegisteredOAuthService(final ServicesManager servicesManager, final String clientId) {
        final Iterator<RegisteredService> it = servicesManager.getAllServices().iterator();
        while (it.hasNext()) {
            final RegisteredService aService = it.next();
            if (aService instanceof OAuthRegisteredService) {
                final OAuthRegisteredService service = (OAuthRegisteredService) aService;
                if (service.getClientId().equals(clientId)) {
                    return service;
                }
            }
        }
        return null;
    }
}
