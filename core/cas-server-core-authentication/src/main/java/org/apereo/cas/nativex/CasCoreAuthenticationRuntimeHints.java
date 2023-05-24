package org.apereo.cas.nativex;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceResponse;
import org.apereo.cas.authentication.credential.AbstractCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasCoreAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.serialization()
            .registerType(AbstractCredential.class)
            .registerType(BasicCredentialMetadata.class)
            .registerType(IPAddressIntelligenceResponse.class)
            .registerType(GeoLocationRequest.class)
            .registerType(GeoLocationResponse.class)
            .registerType(UsernamePasswordCredential.class);

        hints.proxies()
            .registerJdkProxy(AuthenticationEventExecutionPlanConfigurer.class)
            .registerJdkProxy(AuthenticationMetaDataPopulator.class);

        hints.reflection()
            .registerType(UsernamePasswordCredential.class,
                MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INTROSPECT_PUBLIC_METHODS)
            .registerType(SimplePrincipal.class,
                MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(DefaultAuthentication.class,
                MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

    }
}
