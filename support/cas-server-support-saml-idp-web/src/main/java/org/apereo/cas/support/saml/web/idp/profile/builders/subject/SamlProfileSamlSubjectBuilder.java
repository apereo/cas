package org.apereo.cas.support.saml.web.idp.profile.builders.subject;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
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

    private final transient SamlProfileObjectBuilder<NameID> ssoPostProfileSamlNameIdBuilder;

    private final int skewAllowance;

    private final transient SamlObjectEncrypter samlObjectEncrypter;

    public SamlProfileSamlSubjectBuilder(final OpenSamlConfigBean configBean,
                                         final SamlProfileObjectBuilder<NameID> ssoPostProfileSamlNameIdBuilder,
                                         final int skewAllowance,
                                         final SamlObjectEncrypter samlObjectEncrypter) {
        super(configBean);
        this.ssoPostProfileSamlNameIdBuilder = ssoPostProfileSamlNameIdBuilder;
        this.skewAllowance = skewAllowance;
        this.samlObjectEncrypter = samlObjectEncrypter;
    }

    @Override
    public Subject build(final RequestAbstractType authnRequest,
                         final HttpServletRequest request,
                         final HttpServletResponse response,
                         final Object assertion,
                         final SamlRegisteredService service,
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

        val assertion = Assertion.class.cast(casAssertion);
        val validFromDate = ZonedDateTime.ofInstant(assertion.getValidFromDate().toInstant(), ZoneOffset.UTC);
        LOGGER.debug("Locating the assertion consumer service url for binding [{}]", binding);
        @NonNull
        val acs = SamlIdPUtils.determineAssertionConsumerService(authnRequest, adaptor, binding);
        val location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        if (StringUtils.isBlank(location)) {
            LOGGER.warn("Subject recipient is not defined from either authentication request or metadata for [{}]", adaptor.getEntityId());
        }

        val subjectNameId = getNameIdForService(request, response, authnRequest, service, adaptor, binding, assertion, messageContext);
        val subjectConfNameId = service.isSkipGeneratingSubjectConfirmationNameId()
            ? null
            : getNameIdForService(request, response, authnRequest, service, adaptor, binding, assertion, messageContext);

        val subject = newSubject(subjectNameId, subjectConfNameId,
            service.isSkipGeneratingSubjectConfirmationRecipient() ? null : location,
            service.isSkipGeneratingSubjectConfirmationNotOnOrAfter() ? null : validFromDate.plusSeconds(this.skewAllowance),
            service.isSkipGeneratingSubjectConfirmationInResponseTo() ? null : authnRequest.getID(),
            service.isSkipGeneratingSubjectConfirmationNotBefore() ? null : ZonedDateTime.now());

        if (NameIDType.ENCRYPTED.equalsIgnoreCase(subjectNameId.getFormat())) {
            subject.setNameID(null);
            subject.getSubjectConfirmations().forEach(c -> c.setNameID(null));

            val encryptedId = samlObjectEncrypter.encode(subjectNameId, service, adaptor);
            subject.setEncryptedID(encryptedId);

            if (subjectConfNameId != null) {
                val encryptedConfId = samlObjectEncrypter.encode(subjectConfNameId, service, adaptor);
                subject.getSubjectConfirmations().forEach(c -> c.setEncryptedID(encryptedConfId));
            }
        }

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
