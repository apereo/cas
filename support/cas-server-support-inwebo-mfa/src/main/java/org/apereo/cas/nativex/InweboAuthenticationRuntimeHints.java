package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

/**
 * This is {@link InweboAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class InweboAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        val list = List.of(
            XIncludeAwareParserConfiguration.class,
            DTDDVFactoryImpl.class,
            SaajSoapMessageFactory.class,
            HttpUrlConnectionMessageSender.class,
            SoapFaultMessageResolver.class,
            "com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace"
        );
        registerReflectionHints(hints, list);
        var resource = new ClassPathResource("org/springframework/ws/transport/http/MessageDispatcherServlet.properties", classLoader);
        hints.resources().registerResource(resource);
        resource = new ClassPathResource("org/springframework/ws/client/core/WebServiceTemplate.properties", classLoader);
        hints.resources().registerResource(resource);
    }

}
