package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.CasProtocolConstants;
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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.SubjectLocality;

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
    private final transient AuthnContextClassRefBuilder authnContextClassRefBuilder;
    private final CasConfigurationProperties casProperties;

    public SamlProfileSamlAuthNStatementBuilder(final OpenSamlConfigBean configBean,
                                                final AuthnContextClassRefBuilder authnContextClassRefBuilder,
                                                final CasConfigurationProperties casProperties) {
        super(configBean);
        this.authnContextClassRefBuilder = authnContextClassRefBuilder;
        this.casProperties = casProperties;
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
        return buildAuthnStatement(assertion, authnRequest, adaptor, service, binding, request);
    }

    /**
     * Creates an authentication statement for the current request.
     *
     * @param casAssertion   the cas assertion
     * @param authnRequest   the authn request
     * @param adaptor        the adaptor
     * @param service        the service
     * @param binding        the binding
     * @param request        the request
     * @return constructed authentication statement
     * @throws SamlException the saml exception
     */
    private AuthnStatement buildAuthnStatement(final Object casAssertion,
                                               final RequestAbstractType authnRequest,
                                               final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                               final SamlRegisteredService service,
                                               final String binding,
                                               final HttpServletRequest request) throws SamlException {
        val assertion = Assertion.class.cast(casAssertion);
        val authenticationMethod = this.authnContextClassRefBuilder.build(assertion, authnRequest, adaptor, service);
        var id = request != null ? CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET) : StringUtils.EMPTY;
        if (StringUtils.isBlank(id)) {
            LOGGER.warn("Unable to locate service ticket as the session index; Generating random identifier instead...");
            id = '_' + String.valueOf(RandomUtils.nextLong());
        }
        val statement = newAuthnStatement(authenticationMethod, DateTimeUtils.zonedDateTimeOf(assertion.getAuthenticationDate()), id);
        if (assertion.getValidUntilDate() != null) {
            val dt = DateTimeUtils.zonedDateTimeOf(assertion.getValidUntilDate());

            val skewAllowance = service.getSkewAllowance() > 0
                ? service.getSkewAllowance()
                : casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance();
            statement.setSessionNotOnOrAfter(dt.plusSeconds(skewAllowance).toInstant());
        }
        val subjectLocality = buildSubjectLocality(assertion, authnRequest, adaptor, binding);
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
    protected SubjectLocality buildSubjectLocality(final Assertion assertion, final RequestAbstractType authnRequest,
                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                   final String binding) throws SamlException {
        val subjectLocality = newSamlObject(SubjectLocality.class);
        val hostAddress = InetAddressUtils.getCasServerHostAddress(casProperties.getServer().getName());
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
        LOGGER.debug("Built subject locality address [{}] for the saml authentication statement prepped for [{}]", hostAddress, issuer);
        subjectLocality.setAddress(hostAddress);
        return subjectLocality;
    }
}
