package org.apereo.cas.support.saml.web.idp.profile.builders.conditions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * This is {@link SamlProfileSamlConditionsBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlConditionsBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Conditions> {
    private static final long serialVersionUID = 126393045912318783L;

    private final CasConfigurationProperties casProperties;

    public SamlProfileSamlConditionsBuilder(final OpenSamlConfigBean configBean, final CasConfigurationProperties casProperties) {
        super(configBean);
        this.casProperties = casProperties;
    }

    @Override
    public Conditions build(final RequestAbstractType authnRequest, final HttpServletRequest request,
                            final HttpServletResponse response,
                            final Object assertion, final SamlRegisteredService service,
                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                            final String binding, final MessageContext messageContext) throws SamlException {
        return buildConditions(authnRequest, assertion, service, adaptor, messageContext);
    }

    /**
     * Build conditions conditions.
     *
     * @param authnRequest   the authn request
     * @param assertion      the assertion
     * @param service        the service
     * @param adaptor        the adaptor
     * @param messageContext the message context
     * @return the conditions
     * @throws SamlException the saml exception
     */
    protected Conditions buildConditions(final RequestAbstractType authnRequest,
                                         final Object assertion, final SamlRegisteredService service,
                                         final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                         final MessageContext messageContext) throws SamlException {

        val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);

        var skewAllowance = service.getSkewAllowance() > 0
            ? service.getSkewAllowance()
            : casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance();
        if (skewAllowance <= 0) {
            skewAllowance = casProperties.getSamlCore().getSkewAllowance();
        }

        val audienceUrls = new ArrayList<String>(2);
        audienceUrls.add(adaptor.getEntityId());
        if (StringUtils.isNotBlank(service.getAssertionAudiences())) {
            val audiences = org.springframework.util.StringUtils.commaDelimitedListToSet(service.getAssertionAudiences());
            audienceUrls.addAll(audiences);
        }
        return newConditions(currentDateTime.minusSeconds(skewAllowance),
            currentDateTime.plusSeconds(skewAllowance),
            audienceUrls.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }
}
