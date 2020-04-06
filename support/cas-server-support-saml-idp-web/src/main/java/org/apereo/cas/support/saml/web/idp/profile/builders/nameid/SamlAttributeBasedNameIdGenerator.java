package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.profile.AbstractSAML2NameIDGenerator;
import org.opensaml.saml.saml2.profile.SAML2NameIDGenerator;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * This is {@link SamlAttributeBasedNameIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class SamlAttributeBasedNameIdGenerator extends AbstractSAML2NameIDGenerator {
    private final String attributeValue;

    /**
     * Gets generator for.
     *
     * @param authnRequest   the authn request
     * @param nameFormat     the name format
     * @param service        the service
     * @param attributeValue the attribute value
     * @return the generator for
     */
    @SneakyThrows
    public static SAML2NameIDGenerator get(final Optional<RequestAbstractType> authnRequest,
                                           final String nameFormat,
                                           final SamlRegisteredService service,
                                           final String attributeValue) {
        val encoder = new SamlAttributeBasedNameIdGenerator(attributeValue);
        encoder.setId(SamlAttributeBasedNameIdGenerator.class.getSimpleName());
        encoder.setFormat(nameFormat);
        encoder.setDefaultIdPNameQualifierLookupStrategy(baseContexts -> service.getNameIdQualifier());
        encoder.setDefaultSPNameQualifierLookupStrategy(baseContexts -> service.getServiceProviderNameIdQualifier());

        authnRequest.ifPresent(request -> {
            SamlIdPUtils.getNameIDPolicy(request).ifPresent(policy -> {
                val qualifier = policy.getSPNameQualifier();
                LOGGER.debug("NameID SP qualifier is set to [{}]", qualifier);
                encoder.setSPNameQualifier(qualifier);
            });
        });
        encoder.setIdPNameQualifier(service.getNameIdQualifier());
        encoder.setOmitQualifiers(service.isSkipGeneratingNameIdQualifiers());
        encoder.initialize();
        return encoder;
    }

    @Override
    protected String getIdentifier(@Nonnull final ProfileRequestContext profileRequestContext) {
        return this.attributeValue;
    }
}
