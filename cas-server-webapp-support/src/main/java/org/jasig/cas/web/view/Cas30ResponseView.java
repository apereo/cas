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

import org.jasig.cas.CasProtocolConstants;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

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
public class Cas30ResponseView extends Cas20ResponseView {

    /**
     * Represents the collection of attributes in the view.
     */
    public static final String MODEL_ATTRIBUTE_NAME_ATTRIBUTES = "attributes";

    /**
     * Represents the authentication date object in the view.
     */
    public static final String MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE = "authenticationDate";

    /**
     * Represents the flag to note whether assertion is backed by new login.
     */
    public static final String MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN = "isFromNewLogin";


    /**
     * Instantiates a new Abstract cas jstl view.
     *
     * @param view the view
     */
    protected Cas30ResponseView(final AbstractUrlBasedView view) {
        super(view);
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {

        super.prepareMergedOutputModel(model, request, response);
        super.putIntoModel(model, MODEL_ATTRIBUTE_NAME_ATTRIBUTES, getPrincipalAttributesAsMultiValuedAttributes(model));
        super.putIntoModel(model, MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE, getAuthenticationDate(model));
        super.putIntoModel(model, MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN, isAssertionBackedByNewLogin(model));
        super.putIntoModel(model, CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                isRememberMeAuthentication(model));
    }
}
