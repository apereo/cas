package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.DefaultSaml20AttributeBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.messaging.context.MessageContext;
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
    private final MessageContext messageContext;
    private final SamlObjectEncrypter samlObjectEncrypter;

    @Override
    public void build(final AttributeStatement attrStatement, final Attribute attribute) {
        if (service.isEncryptAttributes()) {
            val encryptedAttribute = samlObjectEncrypter.encode(attribute, service, adaptor);
            attrStatement.getEncryptedAttributes().add(encryptedAttribute);
            return;
        }

        super.build(attrStatement, attribute);
    }
}
