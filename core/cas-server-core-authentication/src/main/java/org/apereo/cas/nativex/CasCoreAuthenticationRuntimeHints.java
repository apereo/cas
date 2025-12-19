package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationAccountStateHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationPreProcessor;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PrePostAuthenticationHandler;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalElectionStrategyConflictResolver;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceResponse;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.metadata.CacheCredentialsCipherExecutor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.validation.ValidationResponseType;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link CasCoreAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints, List.of(
            IPAddressIntelligenceResponse.class,
            GeoLocationRequest.class,
            GeoLocationResponse.class,
            DefaultAuthenticationHandlerExecutionResult.class,
            ValidationResponseType.class
        ));

        val subclassesInPackage = findSubclassesInPackage(Principal.class, CentralAuthenticationService.NAMESPACE);
        subclassesInPackage.addAll(findSubclassesInPackage(MessageDescriptor.class, CentralAuthenticationService.NAMESPACE));
        subclassesInPackage.addAll(findSubclassesInPackage(CredentialMetadata.class, CentralAuthenticationService.NAMESPACE));
        subclassesInPackage.addAll(findSubclassesInPackage(Authentication.class, CentralAuthenticationService.NAMESPACE));
        subclassesInPackage.addAll(findSubclassesInPackage(AuthenticationHandlerExecutionResult.class, CentralAuthenticationService.NAMESPACE));
        subclassesInPackage.addAll(findSubclassesInPackage(Credential.class, CentralAuthenticationService.NAMESPACE));
        registerSerializationHints(hints, subclassesInPackage);

        val credentials = findSubclassesInPackage(Credential.class, CentralAuthenticationService.NAMESPACE);
        registerReflectionHints(hints, credentials);

        registerSpringProxyHints(hints, PrePostAuthenticationHandler.class, AuthenticationHandler.class);
        registerSpringProxyHints(hints, WebApplicationService.class);

        registerProxyHints(hints, List.of(
            AuthenticationMetaDataPopulator.class,
            AuthenticationAccountStateHandler.class,
            AuthenticationEventExecutionPlanConfigurer.class,
            PrincipalElectionStrategyConflictResolver.class,
            PrincipalElectionStrategy.class,
            CredentialMetadata.class,
            AuthenticationHandler.class,
            AuthenticationPostProcessor.class,
            AuthenticationPreProcessor.class,
            AuthenticationTransactionManager.class,
            AuthenticationHandlerResolver.class));

        registerReflectionHints(hints,
            List.of(
                CacheCredentialsCipherExecutor.class,
                SimplePrincipal.class,
                DefaultAuthentication.class));

        registerSpringProxyHints(hints, AttributeDefinitionStore.class, DisposableBean.class, AutoCloseable.class);
    }
}
