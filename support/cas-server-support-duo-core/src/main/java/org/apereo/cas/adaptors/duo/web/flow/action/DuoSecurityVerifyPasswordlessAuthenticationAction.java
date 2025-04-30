package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This is {@link DuoSecurityVerifyPasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class DuoSecurityVerifyPasswordlessAuthenticationAction extends DuoSecurityAuthenticationWebflowAction {
    private final AuthenticationSystemSupport authenticationSystemSupport;

    public DuoSecurityVerifyPasswordlessAuthenticationAction(
        final AuthenticationSystemSupport authenticationSystemSupport,
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
        final TenantExtractor tenantExtractor) {
        super(duoAuthenticationWebflowEventResolver, tenantExtractor);
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val beanFactory = ((ConfigurableApplicationContext) requestContext.getActiveFlow().getApplicationContext()).getBeanFactory();
        val providers = new ArrayList<>(BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, DuoSecurityMultifactorAuthenticationProvider.class).values());
        return providers
            .stream()
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .filter(provider -> provider.getDuoAuthenticationService().getProperties().isPasswordlessAuthenticationEnabled())
            .map(provider -> FunctionUtils.doAndHandle(() -> {
                val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
                val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(account.getUsername());
                val credential = new DuoSecurityDirectCredential(principal, provider.getId());
                val credentialMetadata = new BasicCredentialMetadata(credential);
                credentialMetadata.setTenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(null));
                
                val service = WebUtils.getService(requestContext);
                val authenticationResultBuilder = authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credential);
                val authenticationResult = authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
                WebUtils.putAuthenticationResultBuilder(authenticationResultBuilder, requestContext);
                WebUtils.putAuthenticationResult(authenticationResult, requestContext);
                WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
                WebUtils.putCredential(requestContext, credential);
                return success();
            }, e -> error()).get())
            .findFirst()
            .orElseGet(this::error);
    }
}
