package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.AbstractCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link DefaultCasWebflowCredentialProvider}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultCasWebflowCredentialProvider implements CasWebflowCredentialProvider {
    protected final TenantExtractor tenantExtractor;

    @Override
    @Nonnull
    public List<Credential> extract(final RequestContext requestContext) {
        val credential = WebUtils.getCredential(requestContext);
        if (credential instanceof final AbstractCredential ac) {
            if (ac.getCredentialMetadata() == null) {
                ac.setCredentialMetadata(new BasicCredentialMetadata(credential));
            }
            val credentialMetadata = Objects.requireNonNull(credential).getCredentialMetadata();
            credentialMetadata.setTenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(null));
        }
        return CollectionUtils.wrapList(credential);
    }
}
