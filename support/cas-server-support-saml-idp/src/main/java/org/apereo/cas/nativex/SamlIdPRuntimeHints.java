package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.support.saml.idp.SamlIdPDistributedSessionCookieCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.JsonResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.XMLMessageDecodersMap;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceMessageHandler;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketImpl;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketImpl;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import net.shibboleth.shared.component.DestructableComponent;
import net.shibboleth.shared.component.IdentifiableComponent;
import net.shibboleth.shared.component.IdentifiedComponent;
import net.shibboleth.shared.component.InitializableComponent;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.metadata.IterableMetadataSource;
import org.opensaml.saml.metadata.resolver.BatchMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * This is {@link SamlIdPRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SamlIdPRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
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
        val samlComponents = new LinkedHashSet<Class>();
        samlComponents.addAll(findSubclassesInPackage(MetadataResolver.class,
            "org.opensaml.saml.metadata", CentralAuthenticationService.NAMESPACE));
        samlComponents.addAll(findSubclassesInPackage(SamlIdPMetadataLocator.class, CentralAuthenticationService.NAMESPACE));
        samlComponents.addAll(findSubclassesInPackage(SamlIdPMetadataGenerator.class, CentralAuthenticationService.NAMESPACE));
        samlComponents.addAll(findSubclassesOf(SamlRegisteredServiceMetadataResolver.class));
        samlComponents.addAll(findSubclassesOf(SamlRegisteredServiceMetadataManager.class));
        samlComponents.addAll(findSubclassesOf(Saml20ObjectBuilder.class));
        samlComponents.addAll(findSubclassesOf(XMLMessageDecodersMap.class));
        samlComponents.add(SamlIdPSingleLogoutServiceMessageHandler.class);
        registerReflectionHints(hints, samlComponents);
        samlComponents.forEach(type -> registerSpringProxyHints(hints, ClassUtils.getAllInterfacesForClass(type)));

        registerSpringProxyHints(hints, BatchMetadataResolver.class, IterableMetadataSource.class, MetadataResolver.class,
            IdentifiableComponent.class, IdentifiedComponent.class, DestructableComponent.class, InitializableComponent.class);
        registerSpringProxyHints(hints, InitializingBean.class, SamlIdPMetadataGenerator.class);
        registerProxyHints(hints, SamlRegisteredServiceMetadataResolver.class, SamlIdPMetadataGenerator.class, SamlIdPMetadataLocator.class);
        registerProxyHints(hints, SamlRegisteredServiceMetadataResolutionPlanConfigurer.class);
        registerSpringProxyHints(hints, DisposableBean.class, SamlRegisteredServiceMetadataResolver.class);
        registerSpringProxyHints(hints, SamlRegisteredServiceMetadataManager.class, SamlRegisteredServiceMetadataResolver.class);
    }
}
