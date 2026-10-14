package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mcp.tools.CasPersonDirectoryTools;
import org.apereo.cas.mcp.tools.CasServiceManagementTools;
import org.apereo.cas.mcp.tools.CasTicketRegistryTools;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasMcpAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AI, module = "mcp")
@AutoConfiguration
public class CasMcpAutoConfiguration {

    @Bean
    public CasServiceManagementTools casServiceManagementTools(
        @Qualifier(ServicesManager.BEAN_NAME) 
        final ServicesManager servicesManager) {
        return new CasServiceManagementTools(servicesManager);
    }

    @Bean
    public CasTicketRegistryTools casTicketRegistryTools(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return new CasTicketRegistryTools(ticketRegistry);
    }

    @Bean
    public CasPersonDirectoryTools casPersonDirectoryTools(
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver principalResolver) {
        return new CasPersonDirectoryTools(principalResolver);
    }
    
}
