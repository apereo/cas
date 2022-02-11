package org.apereo.cas.support.saml.web.idp.profile.builders.subject;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;

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
    private static final long serialVersionUID = 4782621942035583007L;

    private final SamlProfileObjectBuilder<SAMLObject> ssoPostProfileSamlNameIdBuilder;

    private final CasConfigurationProperties casProperties;

    private final transient SamlIdPObjectEncrypter samlObjectEncrypter;

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
        val subjectConfNameId = context.getRegisteredService().isSkipGeneratingSubjectConfirmationNameId()
            ? null
            : getNameIdForService(context);

        val notOnOrAfter = context.getRegisteredService().isSkipGeneratingSubjectConfirmationNotOnOrAfter()
            ? null
            : validFromDate.plusSeconds(context.getRegisteredService().getSkewAllowance() > 0
            ? context.getRegisteredService().getSkewAllowance()
            : Beans.newDuration(casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance()).toSeconds());

        val finalSubjectNameId = encryptNameIdIfNecessary(subjectNameId, context);
        val finalSubjectConfigNameId = encryptNameIdIfNecessary(subjectConfNameId, context);

        val subject = newSubject(finalSubjectNameId, finalSubjectConfigNameId,
            context.getRegisteredService().isSkipGeneratingSubjectConfirmationRecipient() ? null : location,
            notOnOrAfter,
            context.getRegisteredService().isSkipGeneratingSubjectConfirmationInResponseTo() ? null : context.getSamlRequest().getID(),
            context.getRegisteredService().isSkipGeneratingSubjectConfirmationNotBefore() ? null : ZonedDateTime.now(ZoneOffset.UTC));
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
            && subjectNameId instanceof NameID
            && NameIDType.ENCRYPTED.equalsIgnoreCase(((NameID) subjectNameId).getFormat())) {
            return samlObjectEncrypter.encode((NameID) subjectNameId, context.getRegisteredService(), context.getAdaptor());
        }
        return subjectNameId;
    }
}
