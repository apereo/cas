package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoSecurityDirectAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class DuoSecurityDirectAuthenticationAction extends AbstractMultifactorAuthenticationAction<DuoSecurityMultifactorAuthenticationProvider> {
    protected final TenantExtractor tenantExtractor;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val credential = new DuoSecurityDirectCredential(resolvePrincipal(authentication.getPrincipal(), requestContext), provider.getId());
        val credentialMetadata = new BasicCredentialMetadata(credential);
        credentialMetadata.setTenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(null));
        credential.setCredentialMetadata(credentialMetadata);
        WebUtils.putCredential(requestContext, credential);
        return success();
    }
}
