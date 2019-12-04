package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.DefaultSaml20AttributeBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;

/**
 * This is {@link SamlProfileSamlRegisteredServiceAttributeBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SamlProfileSamlRegisteredServiceAttributeBuilder extends DefaultSaml20AttributeBuilder {
    private final SamlRegisteredService service;

    private final SamlRegisteredServiceServiceProviderMetadataFacade adaptor;

    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    @Override
    @SneakyThrows
    public void build(final AttributeStatement attrStatement, final Attribute attribute) {
        if (!service.isEncryptAttributes() || !shouldEncryptAttribute(attribute)) {
            LOGGER.debug("Service [{}] is configured to not encrypt attributes for [{}]", service.getName(), attribute.getName());
            super.build(attrStatement, attribute);
            return;
        }

        val encryptedAttribute = samlObjectEncrypter.encode(attribute, service, adaptor);
        if (encryptedAttribute != null) {
            LOGGER.debug("Encrypted attribute [{}] for service [{}]", attribute.getName(), service.getName());
            attrStatement.getEncryptedAttributes().add(encryptedAttribute);
        } else {
            LOGGER.debug("Unable to encrypt attribute [{}] for service [{}]", attribute.getName(), service.getName());
            super.build(attrStatement, attribute);
        }
    }

    private boolean shouldEncryptAttribute(final Attribute attribute) {
        val encryptableAttributes = service.getEncryptableAttributes();
        if (encryptableAttributes == null || encryptableAttributes.isEmpty() || encryptableAttributes.contains("*")) {
            LOGGER.debug("No explicit attribute encryption rules are defined; Attribute [{}] is selected for encryption.", attribute.getName());
            return true;
        }
        if (encryptableAttributes.contains(attribute.getName())) {
            LOGGER.debug("Attribute encryption rules allow [{}] to be encrypted", attribute.getName());
            return true;
        }
        LOGGER.debug("Skipping encryption as attribute encryption rules do NOT allow [{}] to be encrypted", attribute.getName());
        return false;
    }
}
