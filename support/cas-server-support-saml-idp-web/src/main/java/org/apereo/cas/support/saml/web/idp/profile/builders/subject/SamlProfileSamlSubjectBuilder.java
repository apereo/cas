package org.apereo.cas.support.saml.web.idp.profile.builders.subject;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Subject;

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
@Slf4j
public class SamlProfileSamlSubjectBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Subject> {
    private static final long serialVersionUID = 4782621942035583007L;

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
                         final String binding,
                         final MessageContext messageContext) throws SamlException {
        return buildSubject(request, response, authnRequest, assertion, service, adaptor, binding, messageContext);
    }

    private Subject buildSubject(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final RequestAbstractType authnRequest,
                                 final Object casAssertion,
                                 final SamlRegisteredService service,
                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                 final String binding,
                                 final MessageContext messageContext) throws SamlException {

        final var assertion = Assertion.class.cast(casAssertion);
        final var validFromDate = ZonedDateTime.ofInstant(assertion.getValidFromDate().toInstant(), ZoneOffset.UTC);
        LOGGER.debug("Locating the assertion consumer service url for binding [{}]", binding);
        @NonNull
        final var acs = SamlIdPUtils.determineAssertionConsumerService(authnRequest, adaptor, binding);
        final var location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        if (StringUtils.isBlank(location)) {
            LOGGER.warn("Subject recipient is not defined from either authentication request or metadata for [{}]", adaptor.getEntityId());
        }

        final var nameId = getNameIdForService(request, response, authnRequest, service, adaptor, binding, assertion, messageContext);
        final var subject = newSubject(nameId,
            service.isSkipGeneratingSubjectConfirmationRecipient() ? null : location,
            service.isSkipGeneratingSubjectConfirmationNotOnOrAfter() ? null : validFromDate.plusSeconds(this.skewAllowance),
            service.isSkipGeneratingSubjectConfirmationInResponseTo() ? null : authnRequest.getID(),
            service.isSkipGeneratingSubjectConfirmationNotBefore() ? null : ZonedDateTime.now());

        LOGGER.debug("Created SAML subject [{}]", subject);
        return subject;
    }

    private NameID getNameIdForService(final HttpServletRequest request, final HttpServletResponse response, final RequestAbstractType authnRequest,
                                       final SamlRegisteredService service, final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                       final String binding, final Assertion assertion, final MessageContext messageContext) {
        if (service.isSkipGeneratingAssertionNameId()) {
            LOGGER.warn("Assertion will skip assigning/generating a nameId based on service [{}]", service);
            return null;
        }
        return this.ssoPostProfileSamlNameIdBuilder.build(authnRequest, request, response, assertion, service, adaptor, binding, messageContext);
    }
}
