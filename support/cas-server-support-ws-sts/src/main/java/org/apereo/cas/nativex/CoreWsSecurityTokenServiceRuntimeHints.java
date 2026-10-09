package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapAddress;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapBinding;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapBody;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapFault;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapHeader;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapHeaderFault;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapOperation;
import org.apache.wss4j.common.saml.OpenSAMLUtil;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import jakarta.xml.bind.annotation.W3CDomHandler;

/**
 * This is {@link CoreWsSecurityTokenServiceRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CoreWsSecurityTokenServiceRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.resources().registerResourceBundle("messages.wss4j_errors");
        hints.resources().registerResourceBundle("org.apache.xml.security.resource.xmlsecurity");
        hints.resources().registerPattern("wss/wss-config.xml");
        hints.resources().registerPattern("security-config.xml");
        val resolver = new PathMatchingResourcePatternResolver(classLoader);
        val bundles = Unchecked.supplier(() -> resolver.getResources("classpath*:org/apache/cxf/**/Messages.properties")).get();
        for (val bundle : bundles) {
            val url = Unchecked.supplier(() -> bundle.getURL().toExternalForm()).get();
            val path = url.substring(url.indexOf("org/apache/cxf/"));
            hints.resources().registerResourceBundle(path.replace(".properties", StringUtils.EMPTY).replace('/', '.'));
        }
        registerReflectionHints(hints, findSubclassesInPackage(Object.class,
            "org.apache.wss4j.binding", "org.apache.xml.security.binding", "org.apache.xml.security.configuration"));
        registerReflectionHintsForConstructors(hints,
            findSubclassesInPackage(Object.class, "org.apache.wss4j.stax.impl", "org.apache.xml.security.stax.impl"));
        registerReflectionHintsForConstructors(hints, List.of(W3CDomHandler.class));
        registerReflectionHints(hints, OpenSAMLUtil.class);
        hints.resources().registerPattern("META-INF/cxf/**");
        hints.resources().registerPattern("wsdl/**");
        hints.resources().registerPattern("schemas/**");
        registerReflectionHints(hints, findSubclassesInPackage(Object.class, "com.ibm.wsdl"));
        for (val adapter : List.of(SoapAddress.class, SoapBinding.class, SoapBody.class, SoapFault.class,
            SoapHeader.class, SoapHeaderFault.class, SoapOperation.class)) {
            registerProxyHints(hints, adapter);
            registerReflectionHints(hints, adapter);
        }
        val extensions = Unchecked.supplier(() -> resolver.getResources("classpath*:META-INF/cxf/bus-extensions.txt")).get();
        for (val extension : extensions) {
            val content = Unchecked.supplier(() -> extension.getContentAsString(StandardCharsets.UTF_8)).get();
            content.lines()
                .map(String::trim)
                .filter(line -> StringUtils.isNotBlank(line) && !line.startsWith("#"))
                .map(line -> StringUtils.substringBefore(line, ':'))
                .forEach(type -> registerReflectionHints(hints, List.of(type)));
        }
    }
}
