package org.jasig.cas.mgmt.services.audit;

import org.aspectj.lang.JoinPoint;
import org.jasig.cas.web.support.WebUtils;
import org.jasig.inspektr.common.spi.PrincipalResolver;

/**
 * Principal resolver for inspektr based on pac4j.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
public class Pac4jAuditablePrincipalResolver  implements PrincipalResolver {

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

    private String getFromSecurityContext() {
        return WebUtils.getAuthenticatedUsername();
    }

}
