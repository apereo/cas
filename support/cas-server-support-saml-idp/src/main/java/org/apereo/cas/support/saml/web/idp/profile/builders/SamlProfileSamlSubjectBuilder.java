package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.client.validation.Assertion;
import org.apereo.cas.support.saml.SamlException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileSamlSubjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlSubjectBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Subject> {
    private static final long serialVersionUID = 4782621942035583007L;
    
    private SamlProfileSamlNameIdBuilder ssoPostProfileSamlNameIdBuilder;
    
    private int skewAllowance;
    
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
        final NameID nameID = this.ssoPostProfileSamlNameIdBuilder.build(authnRequest, request, response, assertion, service, adaptor);
        final ZonedDateTime validFromDate = ZonedDateTime.ofInstant(assertion.getValidFromDate().toInstant(), ZoneOffset.UTC);
        final Subject subject = newSubject(nameID.getFormat(), nameID.getValue(),
                authnRequest.getAssertionConsumerServiceURL(),
                validFromDate.plusSeconds(this.skewAllowance),
                authnRequest.getID());
        subject.setNameID(nameID);
        return subject;
    }
    
    public void setSsoPostProfileSamlNameIdBuilder(final SamlProfileSamlNameIdBuilder ssoPostProfileSamlNameIdBuilder) {
        this.ssoPostProfileSamlNameIdBuilder = ssoPostProfileSamlNameIdBuilder;
    }

    public void setSkewAllowance(final int skewAllowance) {
        this.skewAllowance = skewAllowance;
    }
}
