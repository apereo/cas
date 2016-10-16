package org.apereo.cas.web.view;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

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
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class Cas30ResponseView extends Cas20ResponseView {

    private boolean releaseProtocolAttributes = true;

    private String authenticationContextAttribute;

    /**
     * Instantiates a new Abstract cas response view.
     */
    public Cas30ResponseView() {
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

        final Optional<MultifactorAuthenticationProvider> contextProvider =
                getSatisfiedMultifactorAuthenticationProvider(model);
        if (contextProvider.isPresent() && StringUtils.isNotBlank(authenticationContextAttribute)) {
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
    protected Map<String, Object> getCasPrincipalAttributes(final Map<String, Object> model,
                                                            final RegisteredService registeredService) {
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
                builder.append(StringEscapeUtils.escapeXml10(value.toString().trim()));
                builder.append("</cas:".concat(k).concat(">"));
                formattedAttributes.add(builder.toString());
            });
        });
        super.putIntoModel(model,
                CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES,
                formattedAttributes);
    }

    public void setReleaseProtocolAttributes(final boolean releaseProtocolAttributes) {
        this.releaseProtocolAttributes = releaseProtocolAttributes;
    }

    public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
        this.authenticationContextAttribute = authenticationContextAttribute;
    }
}
