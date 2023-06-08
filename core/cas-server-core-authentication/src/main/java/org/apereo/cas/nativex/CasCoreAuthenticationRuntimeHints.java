package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationAccountStateHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalElectionStrategyConflictResolver;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceResponse;
import org.apereo.cas.authentication.metadata.CacheCredentialsCipherExecutor;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.validation.ValidationResponseType;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

import java.util.Collection;
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
            .registerType(ValidationResponseType.class);

        findSubclassesInPackage(WebApplicationService.class, CentralAuthenticationService.NAMESPACE)
            .forEach(el -> hints.serialization().registerType(el));
        findSubclassesInPackage(MessageDescriptor.class, CentralAuthenticationService.NAMESPACE)
            .forEach(el -> hints.serialization().registerType(el));

        val credentials = findSubclassesInPackage(Credential.class, CentralAuthenticationService.NAMESPACE);
        credentials.forEach(el -> hints.serialization().registerType(el));
        registerReflectionHints(hints, credentials);

        hints.proxies()
            .registerJdkProxy(AuthenticationMetaDataPopulator.class)
            .registerJdkProxy(AuthenticationAccountStateHandler.class)
            .registerJdkProxy(AuthenticationEventExecutionPlanConfigurer.class)
            .registerJdkProxy(PrincipalElectionStrategyConflictResolver.class)
            .registerJdkProxy(PrincipalElectionStrategy.class)
            .registerJdkProxy(AuthenticationTransactionManager.class)
            .registerJdkProxy(AuthenticationHandlerResolver.class);

        registerReflectionHints(hints,
            List.of(
                CacheCredentialsCipherExecutor.class,
                SimplePrincipal.class,
                DefaultAuthentication.class));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        entries.forEach(el -> hints.reflection().registerType((Class) el,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS));
    }

}
