package org.apereo.cas.support.saml.web.idp.profile.builders.subject;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlProfileSamlSubjectBuilder.class);

    private final SamlProfileObjectBuilder<NameID> ssoPostProfileSamlNameIdBuilder;

    private final int skewAllowance;

    public SamlProfileSamlSubjectBuilder(final OpenSamlConfigBean configBean,
                                         final SamlProfileObjectBuilder<NameID> ssoPostProfileSamlNameIdBuilder,
                                         final int skewAllowance) {
        super(configBean);
        this.ssoPostProfileSamlNameIdBuilder = ssoPostProfileSamlNameIdBuilder;
        this.skewAllowance = skewAllowance;
    }

    @Override
    public Subject build(final RequestAbstractType authnRequest, final HttpServletRequest request,
                         final HttpServletResponse response,
                         final Object assertion, final SamlRegisteredService service,
                         final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                         final String binding) throws SamlException {
        return buildSubject(request, response, authnRequest, assertion, service, adaptor, binding);
    }

    private Subject buildSubject(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final RequestAbstractType authnRequest,
                                 final Object casAssertion,
                                 final SamlRegisteredService service,
                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                 final String binding) throws SamlException {

        final Assertion assertion = Assertion.class.cast(casAssertion);


        final ZonedDateTime validFromDate = ZonedDateTime.ofInstant(assertion.getValidFromDate().toInstant(), ZoneOffset.UTC);

        final AssertionConsumerService acs = adaptor.getAssertionConsumerService(binding);
        if (acs == null) {
            throw new IllegalArgumentException("Failed to locate the assertion consumer service url for binding " + binding);
        }

        final String location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        if (StringUtils.isBlank(location)) {
            LOGGER.warn("Subject recipient is not defined from either authentication request or metadata for [{}]", adaptor.getEntityId());
        }

        final NameID nameId = getNameIdForService(request, response, authnRequest, service, adaptor, binding, assertion);
        final Subject subject = newSubject(nameId,
                service.isSkipGeneratingSubjectConfirmationRecipient() ? null : location,
                service.isSkipGeneratingSubjectConfirmationNotOnOrAfter() ? null : validFromDate.plusSeconds(this.skewAllowance),
                service.isSkipGeneratingSubjectConfirmationInResponseTo() ? null : authnRequest.getID(),
                service.isSkipGeneratingSubjectConfirmationNotBefore() ? null : ZonedDateTime.now());
        
        LOGGER.debug("Created SAML subject [{}]", subject);
        return subject;
    }

    private NameID getNameIdForService(final HttpServletRequest request, final HttpServletResponse response, final RequestAbstractType authnRequest,
                                       final SamlRegisteredService service, final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                       final String binding, final Assertion assertion) {
        if (service.isSkipGeneratingAssertionNameId()) {
            LOGGER.warn("Assertion will skip assigning/generating a nameId based on service [{}]", service);
            return null;
        }
        return this.ssoPostProfileSamlNameIdBuilder.build(authnRequest, request, response, assertion, service, adaptor, binding);
    }
}
