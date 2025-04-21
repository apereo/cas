package org.apereo.cas.support.saml.web.idp.profile.builders.subject;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.util.InetAddressUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.EncryptedID;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;

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
    private final SamlProfileObjectBuilder<SAMLObject> ssoPostProfileSamlNameIdBuilder;

    private final CasConfigurationProperties casProperties;

    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    public SamlProfileSamlSubjectBuilder(
        final OpenSamlConfigBean configBean,
        final SamlProfileObjectBuilder<SAMLObject> ssoPostProfileSamlNameIdBuilder,
        final CasConfigurationProperties casProperties,
        final SamlIdPObjectEncrypter samlObjectEncrypter) {
        super(configBean);
        this.ssoPostProfileSamlNameIdBuilder = ssoPostProfileSamlNameIdBuilder;
        this.samlObjectEncrypter = samlObjectEncrypter;
        this.casProperties = casProperties;
    }

    @Override
    public Subject build(final SamlProfileBuilderContext context) throws Exception {
        return buildSubject(context);
    }

    private Subject buildSubject(final SamlProfileBuilderContext context) throws Exception {
        val validFromDate = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.trace("Locating the assertion consumer service url for binding [{}]", context.getBinding());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(context.getSamlRequest(), context.getMessageContext()),
            context.getAdaptor(), context.getBinding());
        val location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        val subjectNameId = getNameIdForService(context);

        val registeredService = context.getRegisteredService();
        val subjectConfNameId = registeredService.isSkipGeneratingSubjectConfirmationNameId()
            ? null
            : getNameIdForService(context);

        val notOnOrAfterDateTime = validFromDate.plusSeconds(registeredService.getSkewAllowance() != 0
            ? registeredService.getSkewAllowance()
            : Beans.newDuration(casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance()).toSeconds());
        val notOnOrAfter = registeredService.isSkipGeneratingSubjectConfirmationNotOnOrAfter()
            ? null
            : notOnOrAfterDateTime;
        LOGGER.trace("Subject confirmation notOnOrAfter for service [{}] is [{}]", registeredService.getServiceId(), notOnOrAfter);

        val finalSubjectNameId = encryptNameIdIfNecessary(subjectNameId, context);
        val finalSubjectConfigNameId = encryptNameIdIfNecessary(subjectConfNameId, context);

        val entityId = casProperties.getAuthn().getSamlIdp().getCore().getEntityId();
        val subjectConfirmation = newSubjectConfirmation(
            registeredService.isSkipGeneratingSubjectConfirmationRecipient() ? null : location,
            notOnOrAfter,
            getInResponseTo(context.getSamlRequest(), entityId, registeredService.isSkipGeneratingSubjectConfirmationInResponseTo()),
            registeredService.isSkipGeneratingSubjectConfirmationNotBefore() ? null : ZonedDateTime.now(ZoneOffset.UTC),
            registeredService.isSkipGeneratingSubjectConfirmationAddress() ? null : InetAddressUtils.getByName(location));
        
        val subject = newSubject(finalSubjectNameId, finalSubjectConfigNameId, subjectConfirmation);
        LOGGER.debug("Created SAML subject [{}]", subject);
        return subject;
    }

    private SAMLObject getNameIdForService(final SamlProfileBuilderContext context) throws Exception {
        if (context.getRegisteredService().isSkipGeneratingAssertionNameId()) {
            LOGGER.warn("Assertion will skip assigning/generating a nameId based on service [{}]", context.getRegisteredService());
            return null;
        }
        return ssoPostProfileSamlNameIdBuilder.build(context);
    }

    private SAMLObject encryptNameIdIfNecessary(final SAMLObject subjectNameId,
                                                final SamlProfileBuilderContext context) {
        if (!(subjectNameId instanceof EncryptedID)
            && subjectNameId instanceof final NameID nameId
            && NameIDType.ENCRYPTED.equalsIgnoreCase(nameId.getFormat())) {
            return samlObjectEncrypter.encode(nameId, context.getRegisteredService(), context.getAdaptor());
        }
        return subjectNameId;
    }
}
