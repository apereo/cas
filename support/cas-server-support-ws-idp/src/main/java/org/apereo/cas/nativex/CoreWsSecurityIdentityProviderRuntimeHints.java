package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

/**
 * This is {@link CoreWsSecurityIdentityProviderRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CoreWsSecurityIdentityProviderRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, WSFederationRegisteredService.class);

        List.of(WSFederationRegisteredService.class).forEach(el ->
            hints.reflection().registerType(TypeReference.of(el),
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }
}
