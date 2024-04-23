package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.support.saml.idp.SamlIdPDistributedSessionCookieCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.JsonResourceMetadataResolver;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketImpl;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketImpl;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link SamlIdPRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SamlIdPRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources()
            .registerPattern("metadata/*.xml");

        registerSerializationHints(hints, JsonResourceMetadataResolver.SamlServiceProviderMetadata.class,
            SamlRegisteredService.class,
            SamlArtifactTicketImpl.class,
            SamlAttributeQueryTicketImpl.class);

        registerReflectionHints(hints,
            List.of(
                SamlIdPDistributedSessionCookieCipherExecutor.class,
                SamlRegisteredService.class,
                SamlIdPMetadataGenerator.class,
                SamlIdPMetadataLocator.class
            ));
        registerReflectionHints(hints,
            findSubclassesInPackage(MetadataResolver.class,
                "org.opensaml.saml.metadata", CentralAuthenticationService.NAMESPACE));
        registerReflectionHints(hints,
            findSubclassesInPackage(SamlIdPMetadataLocator.class, CentralAuthenticationService.NAMESPACE));
        registerReflectionHints(hints,
            findSubclassesInPackage(SamlIdPMetadataGenerator.class, CentralAuthenticationService.NAMESPACE));
    }
}
