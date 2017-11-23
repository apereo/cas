package org.apereo.cas.mgmt.services.audit;

import org.apereo.cas.util.Pac4jUtils;
import org.aspectj.lang.JoinPoint;
import org.apereo.inspektr.common.spi.PrincipalResolver;

/**
 * Principal resolver for inspektr based on pac4j.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
public class Pac4jAuditablePrincipalResolver implements PrincipalResolver {

    public Pac4jAuditablePrincipalResolver() {
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        return getFromSecurityContext();
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Exception exception) {
        return getFromSecurityContext();
    }

    @Override
    public String resolve() {
        return getFromSecurityContext();
    }

    private static String getFromSecurityContext() {
        return Pac4jUtils.getPac4jAuthenticatedUsername();
    }

}
