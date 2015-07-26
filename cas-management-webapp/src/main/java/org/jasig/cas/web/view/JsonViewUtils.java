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

package org.jasig.cas.web.view;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class JsonViewUtils {

    /** Private constructor. */
    private JsonViewUtils() {}

    /**
     * Render model and view.
     *
     * @param model the model
     * @param response the response
     * @return the model and view
     */
    public static void render(final Object model, final HttpServletResponse response) {
        try {
            final MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
            final MediaType jsonMimeType = MediaType.APPLICATION_JSON;
            jsonConverter.write(model, jsonMimeType, new ServletServerHttpResponse(response));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

    }
}