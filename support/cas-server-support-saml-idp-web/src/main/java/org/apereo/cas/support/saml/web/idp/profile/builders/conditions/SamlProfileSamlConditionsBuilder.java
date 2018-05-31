package org.apereo.cas.support.saml.web.idp.profile.builders.conditions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlProfileSamlConditionsBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlConditionsBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Conditions> {
    private static final long serialVersionUID = 126393045912318783L;

    @Autowired
    private CasConfigurationProperties casProperties;

    public SamlProfileSamlConditionsBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
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

        final var currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        var skewAllowance = casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance();
        if (skewAllowance <= 0) {
            skewAllowance = casProperties.getSamlCore().getSkewAllowance();
        }

        final List<String> audienceUrls = new ArrayList<>();
        audienceUrls.add(adaptor.getEntityId());
        if (StringUtils.isNotBlank(service.getAssertionAudiences())) {
            final var audiences = org.springframework.util.StringUtils.commaDelimitedListToSet(service.getAssertionAudiences());
            audienceUrls.addAll(audiences);
        }
        final var conditions = newConditions(currentDateTime,
            currentDateTime.plusSeconds(skewAllowance),
            audienceUrls.toArray(new String[]{}));
        return conditions;
    }
}
