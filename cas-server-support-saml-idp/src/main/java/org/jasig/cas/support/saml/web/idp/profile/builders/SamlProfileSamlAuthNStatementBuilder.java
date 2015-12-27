package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.SubjectLocality;
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

    @Override
    public AuthnStatement build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                       final Assertion assertion, final SamlRegisteredService service, final SamlMetadataAdaptor adaptor)
            throws SamlException {
        return buildAuthnStatement(assertion, authnRequest);
    }

    /**
     * Creates an authentication statement for the current request.
     *
     * @return constructed authentication statement
     */
    private AuthnStatement buildAuthnStatement(final Assertion assertion, final AuthnRequest authnRequest)
            throws SamlException {
        final AuthnStatement statement = newAuthnStatement(getAuthenticationMethodFromAssertion(assertion),
                new DateTime(assertion.getAuthenticationDate()));
        if (assertion.getValidUntilDate() != null) {
            statement.setSessionNotOnOrAfter(new DateTime(assertion.getValidUntilDate()));
        }
        statement.setSubjectLocality(buildSubjectLocality(authnRequest));
        return statement;
    }

    private SubjectLocality buildSubjectLocality(final AuthnRequest authnRequest) throws SamlException {
        final SubjectLocality subjectLocality = newSamlObject(SubjectLocality.class);
        subjectLocality.setAddress(authnRequest.getIssuer().getValue());
        return subjectLocality;
    }



    private static String getAuthenticationMethodFromAssertion(final Assertion assertion) {
        final Object object = assertion.getAttributes().get(CasProtocolConstants.VALIDATION_AUTHENTICATION_METHOD_ATTRIBUTE_NAME);
        if (object != null) {
            return object.toString();
        }
        return "";
    }

}
