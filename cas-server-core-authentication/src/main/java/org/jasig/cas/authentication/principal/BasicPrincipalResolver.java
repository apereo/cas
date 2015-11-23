package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Provides the most basic means of principal resolution by mapping
 * {@link org.jasig.cas.authentication.Credential#getId()} onto
 * {@link org.jasig.cas.authentication.principal.Principal#getId()}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("proxyPrincipalResolver")
public class BasicPrincipalResolver implements PrincipalResolver {

    /** Factory to create the principal type. **/
    @NotNull
    private PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    @Override
    public Principal resolve(final Credential credential) {
        return this.principalFactory.createPrincipal(credential.getId());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getId() != null;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }
}
