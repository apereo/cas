package org.apereo.cas.nativex;

import org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl;
import org.apache.xerces.impl.dv.xs.ExtendedSchemaDVFactoryImpl;
import org.apache.xerces.impl.dv.xs.SchemaDVFactoryImpl;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.util.SecurityManager;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.opensaml.core.xml.schema.impl.*;
import org.opensaml.saml.saml1.core.impl.ActionBuilder;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

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
            .registerResourceBundle("org/apache/xerces/impl/xpath/regex/message")
            .registerPattern("schema/*.xsd")
            .registerPattern("saml1-*-config.xml")
            .registerPattern("saml2-*-config.xml")
            .registerPattern("default-config.xml")
            .registerPattern("schema-config.xml");

        List.of(
            org.opensaml.saml.saml1.core.impl.ActionBuilder.class,
            org.opensaml.saml.saml2.core.impl.ActionBuilder.class,
            
            XIncludeAwareParserConfiguration.class,
            SecurityManager.class,
            XSSimpleTypeDecl.class,

            XSAnyBuilder.class,
            XSBase64BinaryBuilder.class,
            XSDateTimeBuilder.class,
            XSStringBuilder.class,
            XSIntegerBuilder.class,
            XSBooleanBuilder.class,
            XSQNameBuilder.class,
            XSURIBuilder.class,

            XSDateTimeMarshaller.class,
            XSStringMarshaller.class,
            XSAnyMarshaller.class,
            XSBooleanMarshaller.class,
            XSQNameMarshaller.class,
            XSBase64BinaryMarshaller.class,
            XSURIMarshaller.class,
            XSIntegerMarshaller.class,

            XSBooleanUnmarshaller.class,
            XSQNameUnmarshaller.class,
            XSStringUnmarshaller.class,
            XSAnyUnmarshaller.class,
            XSIntegerUnmarshaller.class,
            XSURIUnmarshaller.class,
            XSBase64BinaryUnmarshaller.class,
            XSDateTimeUnmarshaller.class,

            ExtendedSchemaDVFactoryImpl.class,
            SchemaDVFactoryImpl.class,
            DTDDVFactoryImpl.class
        )
        .forEach(clazz -> hints.reflection().registerType(clazz,
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
