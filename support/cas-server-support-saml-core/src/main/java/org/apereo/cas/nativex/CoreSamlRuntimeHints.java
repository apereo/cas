package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import net.shibboleth.shared.component.DestructableComponent;
import net.shibboleth.shared.component.InitializableComponent;
import net.shibboleth.shared.resolver.Criterion;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import org.apache.velocity.runtime.ParserPool;
import org.apache.velocity.runtime.Renderable;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.resource.ResourceCache;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.util.introspection.TypeConversionHandler;
import org.apache.velocity.util.introspection.Uberspect;
import org.jspecify.annotations.Nullable;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.Unmarshaller;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CoreSamlRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CoreSamlRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.resources()
            .registerResourceBundle("org/apache/xml/security/resource/xmlsecurity")
            .registerPattern("templates/*.vm")
            .registerPattern("entitydescriptor-criterion-predicate-registry.properties")
            .registerPattern("roledescriptor-criterion-predicate-registry.properties")
            .registerPattern("credential-criteria-registry.properties")
            .registerPattern("org/apache/velocity/runtime/defaults/*.properties")
            .registerPattern("schema/*.xsd")
            .registerPattern("xacml*-config.xml")
            .registerPattern("ws*-config.xml")
            .registerPattern("saml*-config.xml")
            .registerPattern("default-config.xml")
            .registerPattern("schema-config.xml")
            .registerPattern("signature-config.xml")
            .registerPattern("encryption-config.xml")
            .registerPattern("soap11-config.xml");

        registerReflectionHints(hints, findSubclassesInPackage(XMLObjectBuilder.class, "org.opensaml"));
        registerReflectionHints(hints, findSubclassesInPackage(Marshaller.class, "org.opensaml"));
        registerReflectionHints(hints, findSubclassesInPackage(Unmarshaller.class, "org.opensaml"));
        registerReflectionHints(hints, findSubclassesInPackage(Criterion.class, "org.opensaml"));
        registerReflectionHints(hints, findSubclassesInPackage(ResourceManager.class, ResourceManager.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(ResourceLoader.class, ResourceLoader.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(ResourceCache.class, ResourceCache.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(StringResourceRepository.class, StringResourceRepository.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(Directive.class, Directive.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(Parser.class, Parser.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(ParserPool.class, ParserPool.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(Renderable.class, Renderable.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(RuntimeServices.class, RuntimeServices.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(Uberspect.class, Uberspect.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(TypeConversionHandler.class, TypeConversionHandler.class.getPackageName()));

        val list = List.of(
            DestructableComponent.class,
            InitializableComponent.class,
            BasicParserPool.class
        );
        registerReflectionHints(hints, list);

    }
}
