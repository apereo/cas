package org.jasig.cas.web.view;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders and prepares CAS3 views. This view is responsible
 * to simply just prep the base model, and delegates to
 * a the real view to render the final output.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class Cas30ResponseView extends Cas20ResponseView {

    /**
     * Instantiates a new Abstract cas response view.
     *
     * @param view the view
     */
    protected Cas30ResponseView(final View view) {
        super(view);
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        super.prepareMergedOutputModel(model, request, response);

        final Service service = super.getServiceFrom(model);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.putAll(getCasPrincipalAttributes(model, registeredService));
        attributes.putAll(getCasProtocolAuthenticationAttributes(model, registeredService));

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attributes, model, registeredService);
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attributes, model, registeredService);

        putCasResponseAttributesIntoModel(model, attributes, registeredService);
    }

    /**
     * Put cas authentication attributes into model.
     *
     * @param model             the model
     * @param registeredService the registered service
     * @return the cas authentication attributes
     */
    protected Map<String, Object> getCasProtocolAuthenticationAttributes(final Map<String, Object> model,
                                                                 final RegisteredService registeredService) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
                Collections.singleton(getAuthenticationDate(model)));
        attributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
                Collections.singleton(isAssertionBackedByNewLogin(model)));
        attributes.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                Collections.singleton(isRememberMeAuthentication(model)));

        final Map<String, Object> filteredAuthenticationAttributes = new HashMap<>(getAuthenticationAttributes(model));
        filteredAuthenticationAttributes.remove(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        filteredAuthenticationAttributes.remove(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN);
        filteredAuthenticationAttributes.remove(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME);
        attributes.putAll(filteredAuthenticationAttributes);

        return attributes;
    }

    /**
     * Put cas principal attributes into model.
     *
     * @param model             the model
     * @param registeredService the registered service
     * @return the cas principal attributes
     */
    protected Map<String, Object> getCasPrincipalAttributes(final Map<String, Object> model, final RegisteredService registeredService) {
        return super.getPrincipalAttributesAsMultiValuedAttributes(model);
    }

    /**
     * Put cas response attributes into model.
     *
     * @param model             the model
     * @param attributes        the attributes
     * @param registeredService the registered service
     */
    protected void putCasResponseAttributesIntoModel(final Map<String, Object> model,
                                                     final Map<String, Object> attributes, final RegisteredService registeredService) {
        super.putIntoModel(model,
                CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES,
                this.casAttributeEncoder.encodeAttributes(attributes, getServiceFrom(model)));
    }

    /**
     * The type Success.
     */
    @Component("cas3ServiceSuccessView")
    public static class Success extends Cas30ResponseView {
        /**
         * Instantiates a new Success.
         * @param view the view
         */
        @Autowired
        public Success(@Qualifier("cas3SuccessView")
                       final View view) {
            super(view);
            super.setSuccessResponse(true);
        }
    }
}
