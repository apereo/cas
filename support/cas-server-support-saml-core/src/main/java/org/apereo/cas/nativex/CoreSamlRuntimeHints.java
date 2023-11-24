package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import net.shibboleth.shared.component.DestructableComponent;
import net.shibboleth.shared.component.InitializableComponent;
import net.shibboleth.shared.resolver.Criterion;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl;
import org.apache.xerces.impl.dv.xs.ExtendedSchemaDVFactoryImpl;
import org.apache.xerces.impl.dv.xs.SchemaDVFactoryImpl;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.util.SecurityManager;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.Unmarshaller;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link CoreSamlRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CoreSamlRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources()
            .registerResourceBundle("org/apache/xml/security/resource/xmlsecurity")
            .registerResourceBundle("org/apache/xerces/impl/msg/XMLSchemaMessages")
            .registerResourceBundle("org/apache/xerces/impl/xpath/regex/message")
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

        registerReflectionHint(hints,
            findSubclassesInPackage(XMLObjectBuilder.class, "org.opensaml"));
        registerReflectionHint(hints,
            findSubclassesInPackage(Marshaller.class, "org.opensaml"));
        registerReflectionHint(hints,
            findSubclassesInPackage(Unmarshaller.class, "org.opensaml"));
        registerReflectionHint(hints,
            findSubclassesInPackage(Criterion.class, "org.opensaml"));
        registerReflectionHint(hints,
            findSubclassesInPackage(ResourceManager.class, "org.apache.velocity.runtime"));

        val list = List.of(
            DestructableComponent.class,
            InitializableComponent.class,
            XIncludeAwareParserConfiguration.class,
            SecurityManager.class,
            XSSimpleTypeDecl.class,
            BasicParserPool.class,
            ExtendedSchemaDVFactoryImpl.class,
            SchemaDVFactoryImpl.class,
            DTDDVFactoryImpl.class);
        registerReflectionHint(hints, list);
    }

    private static void registerReflectionHint(final RuntimeHints hints, final Collection clazzes) {
        clazzes.forEach(clazz ->
            hints.reflection().registerType((Class) clazz,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
    }
}
