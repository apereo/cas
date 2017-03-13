package org.apereo.cas.support.realm;

import org.apache.cxf.sts.IdentityMapper;
import org.apache.wss4j.common.principal.CustomTokenPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;


/**
 * This is {@link RealmAwareIdentityMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RealmAwareIdentityMapper implements IdentityMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealmAwareIdentityMapper.class);
    
    @Override
    public Principal mapPrincipal(final String sourceRealm, final Principal sourcePrincipal, final String targetRealm) {
        if ("REALMA".equals(sourceRealm)) {
            final String name = sourcePrincipal.getName().toUpperCase();
            LOGGER.info("Principal {} mapped to {}", sourcePrincipal.getName(), name);
            return new CustomTokenPrincipal(name);
        }

        if ("REALMB".equals(sourceRealm)) {
            final String name = sourcePrincipal.getName().toLowerCase();
            LOGGER.info("Principal {} mapped to {}", sourcePrincipal.getName(), name);
            return new CustomTokenPrincipal(name);
        }

        LOGGER.info("The source realm of " + sourceRealm + " is unknown");
        return null;
    }

}
