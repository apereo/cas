package org.apereo.cas.mgmt.authz;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is {@link ChainingAuthorizationGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ChainingAuthorizationGenerator implements AuthorizationGenerator<CommonProfile> {
    private final CasConfigurationProperties casProperties;
    private final List<AuthorizationGenerator<CommonProfile>> genenerators = new ArrayList<>();

    /**
     * Instantiates a new Chaining authorization generator.
     *
     * @param casProperties the cas properties
     */
    public ChainingAuthorizationGenerator(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    public CommonProfile generate(final WebContext webContext, final CommonProfile commonProfile) {
        CommonProfile profile = commonProfile;
        final Iterator<AuthorizationGenerator<CommonProfile>> it = this.genenerators.iterator();

        while (it.hasNext()) {
            final AuthorizationGenerator<CommonProfile> authz = it.next();
            profile = authz.generate(webContext, profile);
        }
        return profile;
    }

    /**
     * Add authorization generator.
     *
     * @param g the generator.
     */
    public void addAuthorizationGenerator(final AuthorizationGenerator<CommonProfile> g) {
        this.genenerators.add(g);
    }
}
