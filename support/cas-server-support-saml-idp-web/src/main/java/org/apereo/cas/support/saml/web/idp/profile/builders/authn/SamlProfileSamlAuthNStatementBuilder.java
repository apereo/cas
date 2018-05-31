package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.RandomUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.SubjectLocality;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileSamlAuthNStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlAuthNStatementBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AuthnStatement> {

    private static final long serialVersionUID = 8761566449790497226L;

    @Autowired
    private CasConfigurationProperties casProperties;

    private final transient AuthnContextClassRefBuilder authnContextClassRefBuilder;

    public SamlProfileSamlAuthNStatementBuilder(final OpenSamlConfigBean configBean,
                                                final AuthnContextClassRefBuilder authnContextClassRefBuilder) {
        super(configBean);
        this.authnContextClassRefBuilder = authnContextClassRefBuilder;
    }

    @Override
    public AuthnStatement build(final RequestAbstractType authnRequest,
                                final HttpServletRequest request,
                                final HttpServletResponse response,
                                final Object assertion,
                                final SamlRegisteredService service,
                                final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                final String binding,
                                final MessageContext messageContext) throws SamlException {
        return buildAuthnStatement(assertion, authnRequest, adaptor, service, binding, messageContext);
    }

    /**
     * Creates an authentication statement for the current request.
     *
     * @param casAssertion   the cas assertion
     * @param authnRequest   the authn request
     * @param adaptor        the adaptor
     * @param service        the service
     * @param binding        the binding
     * @param messageContext the message context
     * @return constructed authentication statement
     * @throws SamlException the saml exception
     */
    private AuthnStatement buildAuthnStatement(final Object casAssertion, final RequestAbstractType authnRequest,
                                               final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                               final SamlRegisteredService service, final String binding,
                                               final MessageContext messageContext) throws SamlException {
        final var assertion = Assertion.class.cast(casAssertion);
        final var authenticationMethod = this.authnContextClassRefBuilder.build(assertion, authnRequest, adaptor, service);
        final var id = '_' + String.valueOf(Math.abs(RandomUtils.getNativeInstance().nextLong()));
        final var statement = newAuthnStatement(authenticationMethod, DateTimeUtils.zonedDateTimeOf(assertion.getAuthenticationDate()), id);
        if (assertion.getValidUntilDate() != null) {
            final var dt = DateTimeUtils.zonedDateTimeOf(assertion.getValidUntilDate());
            statement.setSessionNotOnOrAfter(
                DateTimeUtils.dateTimeOf(dt.plusSeconds(casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance())));
        }
        final var subjectLocality = buildSubjectLocality(assertion, authnRequest, adaptor, binding);
        statement.setSubjectLocality(subjectLocality);
        return statement;
    }

    /**
     * Build subject locality subject locality.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @param binding      the binding
     * @return the subject locality
     * @throws SamlException the saml exception
     */
    protected SubjectLocality buildSubjectLocality(final Object assertion, final RequestAbstractType authnRequest,
                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                   final String binding) throws SamlException {
        final var subjectLocality = newSamlObject(SubjectLocality.class);
        final var hostAddress = InetAddressUtils.getCasServerHostAddress(casProperties.getServer().getName());
        final var issuer = SamlIdPUtils.getIssuerFromSamlRequest(authnRequest);
        LOGGER.debug("Built subject locality address [{}] for the saml authentication statement prepped for [{}]", hostAddress, issuer);
        subjectLocality.setAddress(hostAddress);
        return subjectLocality;
    }
}
