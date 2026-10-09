package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapAddress;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapBinding;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.apache.wss4j.common.saml.OpenSAMLUtil;
import org.apache.xml.security.configuration.ConfigurationType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import jakarta.xml.bind.annotation.W3CDomHandler;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CoreWsSecurityTokenServiceRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class CoreWsSecurityTokenServiceRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CoreWsSecurityTokenServiceRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.resource().forBundle("org.apache.cxf.bus.managers.Messages").test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forBundle("messages.wss4j_errors").test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forBundle("org.apache.xml.security.resource.xmlsecurity").test(hints));
        for (val resource : List.of("wss/wss-config.xml", "security-config.xml", "META-INF/cxf/bus-extensions.txt",
            "wsdl/ws-trust-1.4-service.wsdl", "wsdl/ws-trust-1.4.wsdl",
            "schemas/oasis-200401-wss-wssecurity-secext-1.0.xsd", "schemas/XMLSchema.dtd")) {
            assertTrue(RuntimeHintsPredicates.resource().forResource(resource).test(hints), resource);
        }
        assertTrue(RuntimeHintsPredicates.reflection().onType(ConfigurationType.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SoapBinding.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SoapAddress.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onFieldAccess(OpenSAMLUtil.class.getDeclaredField("builderFactory")).test(hints));
        for (val type : List.of(W3CDomHandler.class, WSDLManagerImpl.class, SoapBindingFactory.class)) {
            assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(type.getDeclaredConstructor()).test(hints), type.getName());
        }
    }
}
