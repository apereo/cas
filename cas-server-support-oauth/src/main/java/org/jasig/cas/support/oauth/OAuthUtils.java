/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.scribe.utils.OAuthEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This class has some usefull methods to output data in plain text, handle redirects or add parameter in url.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthUtils.class);
    
    private OAuthUtils() {
    }
    
    public static ModelAndView writeTextError(HttpServletResponse response, String error) {
        return OAuthUtils.writeText(response, "error=" + error);
    }
    
    public static ModelAndView writeText(HttpServletResponse response, String text) {
        PrintWriter printWriter;
        try {
            printWriter = response.getWriter();
            printWriter.print(text);
        } catch (IOException e) {
            logger.warn("Failed to write to response", e);
        }
        return null;
    }
    
    public static ModelAndView redirectToError(String url, String error) {
        if (StringUtils.isBlank(url)) {
            url = "/";
        }
        return OAuthUtils.redirectTo(OAuthUtils.addParameter(url, "error", error));
    }
    
    public static ModelAndView redirectTo(String url) {
        return new ModelAndView(new RedirectView(url));
    }
    
    public static String addParameter(String url, String name, String value) {
        if (url.indexOf("?") >= 0) {
            return url + "&" + name + "=" + OAuthEncoder.encode(value);
        } else {
            return url + "?" + name + "=" + OAuthEncoder.encode(value);
        }
    }
}
