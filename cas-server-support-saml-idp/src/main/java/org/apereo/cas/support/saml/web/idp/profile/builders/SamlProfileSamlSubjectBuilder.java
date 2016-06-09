package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.client.validation.Assertion;
import org.apereo.cas.support.saml.SamlException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

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
@RefreshScope
@Component("samlProfileSamlSubjectBuilder")
public class SamlProfileSamlSubjectBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Subject> {
    private static final long serialVersionUID = 4782621942035583007L;

    @Autowired
    @Qualifier("samlProfileSamlNameIdBuilder")
    private SamlProfileSamlNameIdBuilder ssoPostProfileSamlNameIdBuilder;

    @Value("${cas.samlidp.response.skewAllowance:0}")
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
}
