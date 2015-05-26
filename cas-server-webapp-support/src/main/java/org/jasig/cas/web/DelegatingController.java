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
package org.jasig.cas.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Delegating controller.
 * Tries to find a controller among its delegates, that can handle the current request.
 * If none is found, an error is generated.
 * @author Frederic Esnault
 * @since 3.5
 */
public class DelegatingController extends AbstractController {

    /** View if Service Ticket Validation Fails. */
    private static final String DEFAULT_ERROR_VIEW_NAME = "casServiceFailureView";

    private List<DelegateController> delegates;

    /** The view to redirect if no delegate can handle the request. */
    @NotNull
    private String failureView = DEFAULT_ERROR_VIEW_NAME;

    /**
     * Handles the request.
     * Ask all delegates if they can handle the current request.
     * The first to answer true is elected as the delegate that will process the request.
     * If no controller answers true, we redirect to the error page.
     * @param request the request to handle
     * @param response the response to write to
     * @return the model and view object
     * @throws Exception if an error occurs during request handling
     */
    @Override
    protected final ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
                                                    throws Exception {
        for (final DelegateController delegate : delegates) {
            if (delegate.canHandle(request, response)) {
                return delegate.handleRequest(request, response);
            }
        }
        return generateErrorView("INVALID_REQUEST", "INVALID_REQUEST", null);
    }

    /**
     * Generate error view based on {@link #setFailureView(String)}.
     *
     * @param code the code
     * @param description the description
     * @param args the args
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code, final String description, final Object[] args) {
        final ModelAndView modelAndView = new ModelAndView(this.failureView);
        final String convertedDescription = getMessageSourceAccessor().getMessage(description, args, description);
        modelAndView.addObject("code", code);
        modelAndView.addObject("description", convertedDescription);

        return modelAndView;
    }

    /**
     * @param delegates the delegate controllers to set
     */
    @NotNull
    public void setDelegates(final List<DelegateController> delegates) {
        this.delegates = delegates;
    }

    /**
     * @param failureView The failureView to set.
     */
    public final void setFailureView(final String failureView) {
        this.failureView = failureView;
    }
}
