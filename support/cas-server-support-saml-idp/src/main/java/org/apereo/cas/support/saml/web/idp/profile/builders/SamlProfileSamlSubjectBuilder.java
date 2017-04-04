package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link SamlProfileSamlSubjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlSubjectBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Subject> {
    private static final long serialVersionUID = 4782621942035583007L;

    private SamlProfileObjectBuilder<NameID> ssoPostProfileSamlNameIdBuilder;

    private int skewAllowance;

    public SamlProfileSamlSubjectBuilder(final OpenSamlConfigBean configBean, final SamlProfileObjectBuilder<NameID> ssoPostProfileSamlNameIdBuilder,
                                         final int skewAllowance) {
        super(configBean);
        this.ssoPostProfileSamlNameIdBuilder = ssoPostProfileSamlNameIdBuilder;
        this.skewAllowance = skewAllowance;
    }

    @Override
    public Subject build(final AuthnRequest authnRequest, final HttpServletRequest request, 
                         final HttpServletResponse response,
                         final Assertion assertion, final SamlRegisteredService service,
                         final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                         final String binding) throws SamlException {
        return buildSubject(request, response, authnRequest, assertion, service, adaptor, binding);
    }

    private Subject buildSubject(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final AuthnRequest authnRequest,
                                 final Assertion assertion,
                                 final SamlRegisteredService service,
                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                 final String binding) throws SamlException {
        final NameID nameID = this.ssoPostProfileSamlNameIdBuilder.build(authnRequest, request, response, 
                assertion, service, adaptor, binding);
        final ZonedDateTime validFromDate = ZonedDateTime.ofInstant(assertion.getValidFromDate().toInstant(), ZoneOffset.UTC);

        final AssertionConsumerService acs = adaptor.getAssertionConsumerService(binding);
        if (acs == null) {
            throw new IllegalArgumentException("Failed to locate the assertion consumer service url");
        }

        final String location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        final Subject subject = newSubject(nameID.getFormat(), nameID.getValue(),
                location, validFromDate.plusSeconds(this.skewAllowance), authnRequest.getID());
        subject.setNameID(nameID);
        return subject;
    }
}
