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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.CasAttributeEncoder;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders and prepares CAS2 views. This view is responsible
 * to simply just prep the base model, and delegates to
 * a the real view to render the final output.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class Cas30ResponseView extends Cas20ResponseView {

    /** The attribute encoder instance. */
    @NotNull
    private CasAttributeEncoder casAttributeEncoder;

    /** The Services manager. */
    @NotNull
    private ServicesManager servicesManager;

    /**
     * Instantiates a new Abstract cas response view.
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

        final Map<String, Object> attributes = new HashMap<>(getPrincipalAttributesAsMultiValuedAttributes(model));
        attributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
                Collections.singleton(getAuthenticationDate(model)));
        attributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
                Collections.singleton(isAssertionBackedByNewLogin(model)));
        attributes.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                Collections.singleton(isRememberMeAuthentication(model)));

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attributes, model);

        super.putIntoModel(model,
                CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES,
                this.casAttributeEncoder.encodeAttributes(attributes, getServiceFrom(model)));
    }

    /**
     * Decide if credential password should be released as attribute.
     * The credential must have been cached as an authentication attribute
     * and the attribute release policy must be allowed to release the
     * mock attribute that {@link CasViewConstants#MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL}.
     *
     * @param attributes the attributes
     * @param model the model
     */
    protected void decideIfCredentialPasswordShouldBeReleasedAsAttribute(final Map<String, Object> attributes,
                                                                         final Map<String, Object> model) {
        final String credential = super.getCredentialPasswordFromAuthentication(model);
        if (StringUtils.isNotBlank(credential)) {
            logger.debug("Obtained credential password as an authentication attribute");

            final Service service = super.getServiceFrom(model);
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            final Principal principal = super.getPrincipal(model);
            final Map<String, Object> principalAttrs = registeredService.getAttributeReleasePolicy().getAttributes(principal);
            if (principalAttrs.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL)) {
                logger.debug("Obtained credential password is passed to the CAS payload under [{}]",
                        CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
                attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, Collections.singleton(credential));
            } else {
                logger.debug("Released principal attributes [{}] do not authorize the release of "
                                + "credential password, because the attribute [{}] is missing from the attribute release policy",
                        principalAttrs.keySet(), CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
            }
        } else {
            logger.trace("Credential password is not cached and will not be made available to the response.");
        }
    }

    /**
     * Sets services manager.
     *
     * @param servicesManager the services manager
     * @since 4.1
     */
    public void setServicesManager(@NotNull final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * Sets cas attribute encoder.
     *
     * @param casAttributeEncoder the cas attribute encoder
     * @since 4.1
     */
    public void setCasAttributeEncoder(@NotNull final CasAttributeEncoder casAttributeEncoder) {
        this.casAttributeEncoder = casAttributeEncoder;
    }
}
