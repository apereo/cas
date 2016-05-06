package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.client.validation.Assertion;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

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
@RefreshScope
@Component("samlProfileSamlConditionsBuilder")
public class SamlProfileSamlConditionsBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Conditions> {
    private static final long serialVersionUID = 126393045912318783L;

    @Value("${cas.samlidp.response.skewAllowance:0}")
    private int skewAllowance;

    @Override
    public Conditions build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                                  final Assertion assertion, final SamlRegisteredService service,
                                  final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        return buildConditions(authnRequest, assertion, service, adaptor);
    }

    private Conditions buildConditions(final AuthnRequest authnRequest, final Assertion assertion,
                                       final SamlRegisteredService service,
                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {

        final ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        final Conditions conditions = newConditions(currentDateTime,
                currentDateTime.plusSeconds(this.skewAllowance),
                adaptor.getEntityId());
        return conditions;
    }
}
