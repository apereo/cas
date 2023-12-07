package org.apereo.cas.nativex;

import org.apereo.cas.support.saml.idp.SamlIdPCasEventListener;
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
 * This is {@link SamlIdPCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SamlIdPCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerPattern("template-idp-metadata.vm");
    }
}
