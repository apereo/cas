package org.jasig.cas.web.view;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.support.CasAttributeEncoder;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.web.view.CasViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import javax.annotation.Resource;
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
    @Resource(name="casAttributeEncoder")
    private CasAttributeEncoder casAttributeEncoder;

    /** The Services manager. */
    @NotNull
    @Resource(name="servicesManager")
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

        final Service service = super.getServiceFrom(model);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        final Map<String, Object> attributes = new HashMap<>(getPrincipalAttributesAsMultiValuedAttributes(model));
        attributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
                Collections.singleton(getAuthenticationDate(model)));
        attributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
                Collections.singleton(isAssertionBackedByNewLogin(model)));
        attributes.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                Collections.singleton(isRememberMeAuthentication(model)));

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attributes, model, registeredService);
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attributes, model, registeredService);

        super.putIntoModel(model,
                CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES,
                this.casAttributeEncoder.encodeAttributes(attributes, getServiceFrom(model)));
    }

    /**
     * Decide if credential password should be released as attribute.
     * The credential must have been cached as an authentication attribute
     * and the attribute release policy must be allowed to release the
     * attribute.
     *
     * @param attributes the attributes
     * @param model the model
     * @param service the service
     */
    protected void decideIfCredentialPasswordShouldBeReleasedAsAttribute(final Map<String, Object> attributes,
                                                                         final Map<String, Object> model,
                                                                         final RegisteredService service) {

        final RegisteredServiceAttributeReleasePolicy policy = service.getAttributeReleasePolicy();
        final boolean isAuthorized = policy != null && policy.isAuthorizedToReleaseCredentialPassword();

        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes,
                getAuthenticationAttribute(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL),
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                service, isAuthorized);
    }

    /**
     * Decide if PGT should be released as attribute.
     * The PGT must have been cached as an authentication attribute
     * and the attribute release policy must be allowed to release the
     * attribute.
     *
     * @param attributes the attributes
     * @param model the model
     * @param service the service
     */
    protected void decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(final Map<String, Object> attributes,
                                                                         final Map<String, Object> model,
                                                                         final RegisteredService service) {
        final RegisteredServiceAttributeReleasePolicy policy = service.getAttributeReleasePolicy();
        final boolean isAuthorized = policy != null && policy.isAuthorizedToReleaseProxyGrantingTicket();

        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes,
                getProxyGrantingTicketId(model),
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET,
                service, isAuthorized);
    }

    /**
     * Decide attribute release based on service attribute policy.
     *
     * @param attributes the attributes
     * @param attributeValue the attribute value
     * @param attributeName the attribute name
     * @param service the service
     * @param doesAttributePolicyAllow does attribute policy allow release of this attribute?
     */
    protected void decideAttributeReleaseBasedOnServiceAttributePolicy(final Map<String, Object> attributes,
                                                                       final String attributeValue,
                                                                       final String attributeName,
                                                                       final RegisteredService service,
                                                                       final boolean doesAttributePolicyAllow) {
        if (StringUtils.isNotBlank(attributeValue)) {
            logger.debug("Obtained [{}] as an authentication attribute", attributeName);

            if (doesAttributePolicyAllow) {
                logger.debug("Obtained [{}] is passed to the CAS validation payload", attributeName);
                attributes.put(attributeName, Collections.singleton(attributeValue));
            } else {
                logger.debug("Attribute release policy for [{}] does not authorize the release of [{}]",
                        service.getServiceId(), attributeName);
            }
        } else {
            logger.trace("[{}] is not available and will not be released to the validation response.", attributeName);
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
        public Success(@Qualifier("cas3JstlSuccessView")
                       final AbstractUrlBasedView view) {
            super(view);
            super.setSuccessResponse(true);
        }
    }
}
