package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.util.DateTimeUtils;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileSamlSubjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("samlProfileSamlSubjectBuilder")
public class SamlProfileSamlSubjectBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Subject> {
    private static final long serialVersionUID = 4782621942035583007L;

    @Autowired
    @Qualifier("samlProfileSamlNameIdBuilder")
    private SamlProfileSamlNameIdBuilder ssoPostProfileSamlNameIdBuilder;

    @Override
    public Subject build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                               final Assertion assertion, final SamlRegisteredService service,
                               final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        return buildSubject(request, response, authnRequest, assertion, service, adaptor);
    }

    private Subject buildSubject(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final AuthnRequest authnRequest, final Assertion assertion,
                                 final SamlRegisteredService service,
                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        final NameID nameID = ssoPostProfileSamlNameIdBuilder.build(authnRequest, request, response, assertion, service, adaptor);
        final Subject subject = newSubject(nameID.getFormat(), nameID.getValue(),
                authnRequest.getAssertionConsumerServiceURL(),
                DateTimeUtils.zonedDateTimeOf(assertion.getValidFromDate()),
                authnRequest.getID());
        subject.setNameID(nameID);
        return subject;
    }
}
