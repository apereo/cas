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
import lombok.val;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

import java.util.Collection;
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

        hints.serialization()
            .registerType(JsonResourceMetadataResolver.SamlServiceProviderMetadata.class)
            .registerType(SamlRegisteredService.class)
            .registerType(SamlArtifactTicketImpl.class)
            .registerType(SamlAttributeQueryTicketImpl.class);

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

    private static void registerReflectionHints(final RuntimeHints hints, final Collection clazzes) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS
        };
        clazzes.forEach(entry -> {
            if (entry instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
        });

    }
}
