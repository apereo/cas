package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.XMLMessageDecodersMap;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import lombok.val;
import net.shibboleth.shared.component.DestructableComponent;
import net.shibboleth.shared.component.IdentifiableComponent;
import net.shibboleth.shared.component.IdentifiedComponent;
import net.shibboleth.shared.component.InitializableComponent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.IterableMetadataSource;
import org.opensaml.saml.metadata.resolver.BatchMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.DecoratingProxy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class SamlIdPRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new SamlIdPRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SamlIdPMetadataGenerator.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SamlIdPMetadataLocator.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SamlProfileObjectBuilder.class, Saml20ObjectBuilder.class,
            SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(XMLMessageDecodersMap.class, Map.class,
            Cloneable.class, Serializable.class, SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(DisposableBean.class, SingleLogoutServiceMessageHandler.class,
            SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(InitializingBean.class, SamlIdPMetadataGenerator.class,
            SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(BatchMetadataResolver.class, IterableMetadataSource.class,
            MetadataResolver.class, IdentifiableComponent.class, IdentifiedComponent.class, DestructableComponent.class,
            InitializableComponent.class, SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SamlRegisteredServiceMetadataManager.class,
            SamlRegisteredServiceMetadataResolver.class, SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SamlRegisteredServiceMetadataManager.class,
            SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(SamlRegisteredService.class).test(hints));
    }
}
