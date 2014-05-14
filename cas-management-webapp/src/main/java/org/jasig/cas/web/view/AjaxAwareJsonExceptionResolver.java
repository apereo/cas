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
package org.jasig.cas.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.exception.JsonExceptionResolver;

/**
* Implementation of JsonExceptionResolver that only triages the exception occurred
* for JSON requests.
*
* @author Misagh Moayyed
* @since 4.0.0
*/
public class AjaxAwareJsonExceptionResolver extends JsonExceptionResolver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String ajaxRequestHeaderName ="x-requested-with";
    private String ajaxRequestHeaderValue = "XMLHttpRequest";

    public void setAjaxRequestHeaderName(final String ajaxRequestHeaderName) {
        this.ajaxRequestHeaderName = ajaxRequestHeaderName;
    }

    public void setAjaxRequestHeaderValue(final String ajaxRequestHeaderValue) {
        this.ajaxRequestHeaderValue = ajaxRequestHeaderValue;
    }

    @Override
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {
        final String contentType = request.getHeader(this.ajaxRequestHeaderName);
        if (contentType != null && contentType.equals(this.ajaxRequestHeaderValue)) {
            logger.debug("Handling exception {} for ajax request indicated by header {}",
                    ex.getClass().getName(), this.ajaxRequestHeaderName);
            return super.resolveException(request, response, handler, ex);
        }
        logger.debug(ex.getMessage(), ex);
        return null;
    }

}
