package org.apereo.cas.multitenancy;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.theme.AbstractThemeResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link TenantThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class TenantThemeResolver extends AbstractThemeResolver {
    protected final ObjectProvider<TenantExtractor> tenantExtractor;

    protected final ObjectProvider<ServicesManager> servicesManager;

    protected final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    protected final ObjectProvider<CasConfigurationProperties> casProperties;

    @Override
    public String resolveThemeName(final @Nullable HttpServletRequest request) {
        return tenantExtractor.getObject()
            .extract(request)
            .map(TenantDefinition::getUserInterfacePolicy)
            .filter(policy -> StringUtils.isNotBlank(policy.getThemeName()))
            .map(TenantUserInterfacePolicy::getThemeName)
            .orElseGet(this::getDefaultThemeName);
    }
}
