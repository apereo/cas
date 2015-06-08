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

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.RememberMeCredential;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class to handle retrieving the Assertion from the model.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public abstract class AbstractCasView extends AbstractView {

    /**
     * Indicate whether this view will be generating the success response or not.
     * By default, the view is treated as a failure.
     */
    protected boolean successResponse;

    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Gets the assertion from the model.
     *
     * @param model the model
     * @return the assertion from
     */
    protected final Assertion getAssertionFrom(final Map<String, Object> model) {
        return (Assertion) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION);
    }

    /**
     * Gets the PGT from the model.
     *
     * @param model the model
     * @return the pgt id
     */
    protected final String getProxyGrantingTicketId(final Map<String, Object> model) {
        return (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
    }

    /**
     * Gets the authentication from the model.
     *
     * @param model the model
     * @return the assertion from
     * @since 4.1.0
     */
    protected final Authentication getPrimaryAuthenticationFrom(final Map<String, Object> model) {
        return getAssertionFrom(model).getPrimaryAuthentication();
    }

    /**
     * Gets an authentication attribute from the primary authentication object.
     *
     * @param model the model
     * @param attributeName the attribute name
     * @return the authentication attribute
     */
    protected final String getAuthenticationAttribute(final Map<String, Object> model, final String attributeName) {
        final Authentication authn = getPrimaryAuthenticationFrom(model);
        return (String) authn.getAttributes().get(attributeName);
    }
    /**
     * Gets the principal from the model.
     *
     * @param model the model
     * @return the assertion from
     * @since 4.1.0
     */
    protected final Principal getPrincipal(final Map<String, Object> model) {
        return getPrimaryAuthenticationFrom(model).getPrincipal();
    }

    /**
     * Gets principal attributes.
     * Single-valued attributes are converted to a collection
     * so the review can easily loop through all.
     * @param model the model
     * @return the attributes
     * @see #convertAttributeValuesToMultiValuedObjects(java.util.Map)
     * @since 4.1.0
     */
    protected final Map<String, Object> getPrincipalAttributesAsMultiValuedAttributes(final Map<String, Object> model) {
        return convertAttributeValuesToMultiValuedObjects(getPrincipal(model).getAttributes());
    }

    /**
     * Gets authentication attributes.
     * Single-valued attributes are converted to a collection
     * so the review can easily loop through all.
     * @param model the model
     * @return the attributes
     * @see #convertAttributeValuesToMultiValuedObjects(java.util.Map)
     * @since 4.1.0
     */
    protected final Map<String, Object> getAuthenticationAttributesAsMultiValuedAttributes(final Map<String, Object> model) {
        return convertAttributeValuesToMultiValuedObjects(getPrimaryAuthenticationFrom(model).getAttributes());
    }

    /**
     * Is remember me authentication?
     * looks at the authentication object to find {@link RememberMeCredential#AUTHENTICATION_ATTRIBUTE_REMEMBER_ME}
     * and expects the assertion to also note a new login session.
     * @param model the model
     * @return true if remember-me, false if otherwise.
     */
    protected final boolean isRememberMeAuthentication(final Map<String, Object> model) {
        final Map<String, Object> authnAttributes = getAuthenticationAttributesAsMultiValuedAttributes(model);
        final Collection authnMethod = (Collection) authnAttributes.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        return authnMethod != null && authnMethod.contains(Boolean.TRUE) && isAssertionBackedByNewLogin(model);
    }


    /**
     * Is assertion backed by new login?
     *
     * @param model the model
     * @return true/false.
     */
    protected final boolean isAssertionBackedByNewLogin(final Map<String, Object> model) {
        return getAssertionFrom(model).isFromNewLogin();
    }

    /**
     * Convert attribute values to multi valued objects.
     *
     * @param attributes the attributes
     * @return the map of attributes to return
     */
    private Map<String, Object> convertAttributeValuesToMultiValuedObjects(final Map<String, Object> attributes) {
        final Map<String, Object> attributesToReturn = new HashMap<>();
        final Set<Map.Entry<String, Object>> entries = attributes.entrySet();
        for (final Map.Entry<String, Object> entry : entries) {
            final Object value = entry.getValue();
            if (value instanceof Collection || value instanceof Map || value instanceof Object[]
                    || value instanceof Iterator || value instanceof Enumeration) {
                attributesToReturn.put(entry.getKey(), value);
            } else {
                attributesToReturn.put(entry.getKey(), Collections.singleton(value));
            }
        }
        return attributesToReturn;
    }

    /**
     * Gets authentication date.
     *
     * @param model the model
     * @return the authentication date
     * @since 4.1.0
     */
    protected final Date getAuthenticationDate(final Map<String, Object> model) {
        return getPrimaryAuthenticationFrom(model).getAuthenticationDate();
    }

    /**
     * Gets validated service from the model.
     *
     * @param model the model
     * @return the validated service from
     */
    protected final Service getServiceFrom(final Map<String, Object> model) {
        return (Service) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE);
    }

    /**
     * Gets chained authentications.
     *
     * @param model the model
     * @return the chained authentications
     */
    protected final Collection<Authentication> getChainedAuthentications(final Map<String, Object> model) {
        final List<Authentication> chainedAuthenticationsToReturn = new ArrayList<>();

        final Assertion assertion = getAssertionFrom(model);
        final List<Authentication> chainedAuthentications = assertion.getChainedAuthentications();

        /**
         * Note that the last index in the list always describes the primary authentication
         * event. All others in the chain should denote proxies. Per the CAS protocol,
         * when authentication has proceeded through multiple proxies,
         * the order in which the proxies were traversed MUST be reflected in the response.
         * The most recently-visited proxy MUST be the first proxy listed, and all the
         * other proxies MUST be shifted down as new proxies are added. I
         */
        final int numberAuthenticationsExceptPrimary = chainedAuthentications.size() - 1;
        for (int i = 0; i < numberAuthenticationsExceptPrimary; i++) {
            chainedAuthenticationsToReturn.add(chainedAuthentications.get(i));
        }
        return chainedAuthenticationsToReturn;
    }

    /**
     * Put into model.
     *
     * @param model the model
     * @param key the key
     * @param value the value
     */
    protected final void putIntoModel(final Map<String, Object> model, final String key, final Object value){
        model.put(key, value);
    }

    /**
     * Put all into model.
     *
     * @param model the model
     * @param values the values
     */
    protected final void putAllIntoModel(final Map<String, Object> model, final Map<String, Object> values){
        model.putAll(values);
    }

    /**
     * Sets whether this view functions as a success response.
     *
     * @param successResponse the success response
     * @since 4.1.0
     */
    public final void setSuccessResponse(final boolean successResponse) {
        this.successResponse = successResponse;
    }
}
