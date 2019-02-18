package org.apereo.cas.web.security;

import lombok.val;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

/**
 * This is {@link CasWebSecurityExpressionHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class CasWebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {
    @Override
    protected SecurityExpressionOperations createSecurityExpressionRoot(final Authentication authentication, final FilterInvocation fi) {
        val root = new CasWebSecurityExpressionRoot(authentication, fi);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(new AuthenticationTrustResolverImpl());
        root.setRoleHierarchy(getRoleHierarchy());
        root.setDefaultRolePrefix("ROLE_");
        return root;
    }
}
