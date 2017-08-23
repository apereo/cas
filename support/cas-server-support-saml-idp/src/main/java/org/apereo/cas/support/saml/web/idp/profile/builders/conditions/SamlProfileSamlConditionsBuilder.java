package org.apereo.cas.support.saml.web.idp.profile.builders.conditions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link SamlProfileSamlConditionsBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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
                            final String binding)
            throws SamlException {
        return buildConditions(authnRequest, assertion, service, adaptor);
    }

    /**
     * Build conditions conditions.
     *
     * @param authnRequest the authn request
     * @param assertion    the assertion
     * @param service      the service
     * @param adaptor      the adaptor
     * @return the conditions
     * @throws SamlException the saml exception
     */
    protected Conditions buildConditions(final RequestAbstractType authnRequest,
                                         final Object assertion,
                                         final SamlRegisteredService service,
                                         final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {

        final ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        int skewAllowance = casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance();
        if (skewAllowance <= 0) {
            skewAllowance = casProperties.getSamlCore().getSkewAllowance();
        }

        final Conditions conditions = newConditions(currentDateTime,
                currentDateTime.plusSeconds(skewAllowance),
                adaptor.getEntityId());
        return conditions;
    }
}
