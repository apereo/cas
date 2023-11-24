package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * This is {@link DefaultCasWebflowCredentialProvider}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DefaultCasWebflowCredentialProvider implements CasWebflowCredentialProvider {
    @Override
    @Nonnull
    public List<Credential> extract(final RequestContext requestContext) {
        return CollectionUtils.wrapList(WebUtils.getCredential(requestContext));
    }
}
