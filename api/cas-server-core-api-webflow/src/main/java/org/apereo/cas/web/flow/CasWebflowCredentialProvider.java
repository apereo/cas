package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Credential;
import org.springframework.webflow.execution.RequestContext;
import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * This is {@link CasWebflowCredentialProvider}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface CasWebflowCredentialProvider {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "casWebflowCredentialProvider";

    /**
     * Extract list of credentials from webflow context.
     *
     * @param requestContext the request context
     * @return the list
     */
    @Nonnull
    List<Credential> extract(RequestContext requestContext);
}
