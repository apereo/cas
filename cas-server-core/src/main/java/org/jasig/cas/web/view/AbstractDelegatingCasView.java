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

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Renders and prepares CAS2 views. This view is responsible
 * to simply just prep the base model, and delegates to
 * a the real view to render the final output.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractDelegatingCasView extends AbstractCasView {
   private final View view;

    /**
     * Instantiates a new Abstract cas jstl view.
     *
     * @param view the view
     */
    protected AbstractDelegatingCasView(final View view) {
        this.view = view;
    }

    @Override
    protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        logger.debug("Preparing the output model to render view...");
        prepareMergedOutputModel(model, request, response);

        logger.trace("Prepared output model with objects [{}]. Now rendering view...",
                model.keySet().toArray());
        this.view.render(model, request, response);
    }

    /**
     * Prepare merged output model before final rendering.
     *
     * @param model the model
     * @param request the request
     * @param response the response
     * @throws Exception the exception
     */
    protected abstract void prepareMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                                HttpServletResponse response) throws Exception;

}
