package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.SubjectLocality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileSamlAuthNStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("samlProfileSamlAuthNStatementBuilder")
public class SamlProfileSamlAuthNStatementBuilder extends AbstractSaml20ObjectBuilder
        implements SamlProfileObjectBuilder<AuthnStatement> {

    private static final long serialVersionUID = 8761566449790497226L;

    @Value("${cas.samlidp.response.skewAllowance:0}")
    private int skewAllowance;

    @Autowired
    @Qualifier("defaultAuthnContextClassRefBuilder")
    private AuthnContextClassRefBuilder authnContextClassRefBuilder;

    @Override
    public final AuthnStatement build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                                      final Assertion assertion, final SamlRegisteredService service,
                                      final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        return buildAuthnStatement(assertion, authnRequest, adaptor, service);
    }

    /**
     * Creates an authentication statement for the current request.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @param service      the service
     * @return constructed authentication statement
     * @throws SamlException the saml exception
     */
    private AuthnStatement buildAuthnStatement(final Assertion assertion, final AuthnRequest authnRequest,
                                               final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                               final SamlRegisteredService service)
            throws SamlException {

        final String authenticationMethod = this.authnContextClassRefBuilder.build(assertion, authnRequest, adaptor, service);
        final AuthnStatement statement = newAuthnStatement(authenticationMethod, new DateTime(assertion.getAuthenticationDate()));
        if (assertion.getValidUntilDate() != null) {
            final DateTime dt = new DateTime(assertion.getValidUntilDate());
            statement.setSessionNotOnOrAfter(dt.plusSeconds(this.skewAllowance));
        }
        statement.setSubjectLocality(buildSubjectLocality(assertion, authnRequest, adaptor));
        return statement;
    }

    /**
     * Build subject locality subject locality.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @return the subject locality
     * @throws SamlException the saml exception
     */
    protected SubjectLocality buildSubjectLocality(final Assertion assertion, final AuthnRequest authnRequest,
                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        final SubjectLocality subjectLocality = newSamlObject(SubjectLocality.class);
        subjectLocality.setAddress(authnRequest.getIssuer().getValue());
        return subjectLocality;
    }
}
