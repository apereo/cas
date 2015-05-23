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

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.exception.JsonExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of JsonExceptionResolver that only triages the exception occurred
 * for JSON requests.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
public class AjaxAwareJsonExceptionResolver extends JsonExceptionResolver {

    private String ajaxRequestHeaderName ="x-requested-with";
    private String ajaxRequestHeaderValue = "XMLHttpRequest";

    /**
     * Header name that identifies this request as ajax.
     * @param ajaxRequestHeaderName header name
     */
    public final void setAjaxRequestHeaderName(final String ajaxRequestHeaderName) {
        this.ajaxRequestHeaderName = ajaxRequestHeaderName;
    }

    /**
     * Header value that identifies this request as ajax. 
     * @param ajaxRequestHeaderValue header value
     */
    public final void setAjaxRequestHeaderValue(final String ajaxRequestHeaderValue) {
        this.ajaxRequestHeaderValue = ajaxRequestHeaderValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {

        final String contentType = request.getHeader(this.ajaxRequestHeaderName);
        if (contentType != null && contentType.equals(this.ajaxRequestHeaderValue)) {
            LOGGER.debug("Handling exception {} for ajax request indicated by header {}",
                    ex.getClass().getName(), this.ajaxRequestHeaderName);
            return super.resolveException(request, response, handler, ex);
        } else {
            LOGGER.trace("Unable to resolve exception {} for request. Ajax request header {} not found.",
                    ex.getClass().getName(), this.ajaxRequestHeaderName);
        }
        LOGGER.debug(ex.getMessage(), ex);
        return null;
    }

}
