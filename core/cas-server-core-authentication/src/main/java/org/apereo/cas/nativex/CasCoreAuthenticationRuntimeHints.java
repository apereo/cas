package org.apereo.cas.nativex;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceResponse;
import org.apereo.cas.authentication.credential.AbstractCredential;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.metadata.CacheCredentialsCipherExecutor;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.validation.ValidationResponseType;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

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
            .registerType(IPAddressIntelligenceResponse.class)
            .registerType(GeoLocationRequest.class)
            .registerType(GeoLocationResponse.class)

            .registerType(DefaultAuthentication.class)
            .registerType(SimplePrincipal.class)
            .registerType(DefaultAuthenticationHandlerExecutionResult.class)

            .registerType(AbstractWebApplicationService.class)
            .registerType(SimpleWebApplicationServiceImpl.class)

            .registerType(DefaultMessageDescriptor.class)
            .registerType(PasswordExpiringWarningMessageDescriptor.class)

            .registerType(AbstractCredential.class)
            .registerType(UsernamePasswordCredential.class)
            .registerType(BasicCredentialMetadata.class)
            .registerType(BasicIdentifiableCredential.class)
            .registerType(RememberMeUsernamePasswordCredential.class)
            .registerType(HttpBasedServiceCredential.class)
            .registerType(OneTimePasswordCredential.class)
            .registerType(ValidationResponseType.class);

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

        List.of(CacheCredentialsCipherExecutor.class).forEach(el ->
            hints.reflection().registerType(TypeReference.of(el),
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }

}
