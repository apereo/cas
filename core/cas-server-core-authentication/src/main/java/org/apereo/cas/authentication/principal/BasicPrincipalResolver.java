package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Credential;

/**
 * Provides the most basic means of principal resolution by mapping
 * {@link Credential#getId()} onto
 * {@link Principal#getId()}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class BasicPrincipalResolver implements PrincipalResolver {

    /** Factory to create the principal type. **/
    
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
