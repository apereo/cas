package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link InweboAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class InweboAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        val list = List.of(
            XIncludeAwareParserConfiguration.class,
            DTDDVFactoryImpl.class
        );
        registerReflectionHints(hints, list);
    }

}
