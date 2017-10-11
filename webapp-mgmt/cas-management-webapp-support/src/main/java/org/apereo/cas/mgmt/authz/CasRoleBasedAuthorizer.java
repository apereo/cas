package org.apereo.cas.mgmt.authz;

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is {@link CasRoleBasedAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasRoleBasedAuthorizer extends RequireAnyRoleAuthorizer<CommonProfile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasRoleBasedAuthorizer.class);

    public CasRoleBasedAuthorizer(final List<String> roles) {
        super(roles);
    }

    @Override
    protected boolean check(final WebContext context, final CommonProfile profile, final String element) throws HttpAction {
        LOGGER.debug("Evaluating [{}] against profile [{}]", element, profile);
        final boolean result = super.check(context, profile, element);
        if (!result) {
            LOGGER.warn("Unable to authorize access, since the authenticated profile [{}] does not contain the required role [{}]", profile, element);
        } else {
            LOGGER.debug("Successfully authorized access for profile [{}] having matched required role [{}]", profile, element);
        }
        return result;
    }
}
