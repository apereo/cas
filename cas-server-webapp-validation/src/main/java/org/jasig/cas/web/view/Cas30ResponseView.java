package org.jasig.cas.web.view;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Renders and prepares CAS3 views. This view is responsible
 * to simply just prep the base model, and delegates to
 * a the real view to render the final output.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class Cas30ResponseView extends Cas20ResponseView {

    @Value("${cas.attrs.protocol.release:true}")
    private boolean releaseProtocolAttributes = true;


    @Value("${cas.mfa.authn.ctx.attribute:authnContextClass}")
    private String authenticationContextAttribute;

    /**
     * Instantiates a new Abstract cas response view.
     */
    protected Cas30ResponseView() {
        super();
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        super.prepareMergedOutputModel(model, request, response);

        final Service service = super.getServiceFrom(model);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.putAll(getCasPrincipalAttributes(model, registeredService));
        
        if (this.releaseProtocolAttributes) {
            attributes.putAll(getCasProtocolAuthenticationAttributes(model, registeredService));
        }

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

        final Map<String, Object> filteredAuthenticationAttributes = new HashMap<>(getAuthenticationAttributes(model));

        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
                Collections.singleton(getAuthenticationDate(model)));
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
                Collections.singleton(isAssertionBackedByNewLogin(model)));
        filteredAuthenticationAttributes.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                Collections.singleton(isRememberMeAuthentication(model)));

        final Optional<MultifactorAuthenticationProvider> contextProvider = getSatisfiedMultifactorAuthenticationProvider(model);
        if (contextProvider.isPresent()) {
            filteredAuthenticationAttributes.put(this.authenticationContextAttribute,
                    Collections.singleton(contextProvider.get().getId()));
        }

        return filteredAuthenticationAttributes;
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
                                                     final Map<String, Object> attributes, 
                                                     final RegisteredService registeredService) {
        final Map<String, Object> encodedAttributes = this.casAttributeEncoder.encodeAttributes(attributes, getServiceFrom(model));
        super.putIntoModel(model,
                CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES,
                encodedAttributes);

        final List<String> formattedAttributes = new ArrayList<>(encodedAttributes.size());
        encodedAttributes.forEach((k, v) -> {
            final Set<Object> values = CollectionUtils.convertValueToCollection(v);
            values.forEach(value -> {
                final StringBuilder builder = new StringBuilder();
                builder.append("<cas:".concat(k).concat(">"));
                builder.append(value.toString().trim());
                builder.append("</cas:".concat(k).concat(">"));
                formattedAttributes.add(builder.toString());
            });
        });
        super.putIntoModel(model,
                CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES,
                formattedAttributes);
    }

    /**
     * The type Success.
     */
    @RefreshScope
    @Component("cas3ServiceSuccessView")
    public static class Success extends Cas30ResponseView {
        /**
         * Instantiates a new Success.
         */
        public Success() {
            super();
        }

        @Autowired(required=false)
        @Override
        public void setView(@Qualifier("cas3SuccessView") final View view) {
            super.setView(view);
        }
        
    }
}
