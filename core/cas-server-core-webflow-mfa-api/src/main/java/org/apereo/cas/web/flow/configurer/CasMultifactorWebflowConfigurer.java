package org.apereo.cas.web.flow.configurer;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;

import java.util.List;

/**
 * This is {@link CasMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface CasMultifactorWebflowConfigurer {
    /**
     * Register multifactor provider authentication webflow.
     *
     * @param flow       the flow
     * @param subflowId  the subflow id
     * @param providerId the provider id
     */
    void registerMultifactorProviderAuthenticationWebflow(Flow flow,
                                                          String subflowId,
                                                          String providerId);

    /**
     * Determine the order of the configurer.
     * @return order
     */
    int getOrder();

    /**
     * Collection of flow definition registries that are tied to this mfa flow.
     * @return list of flow definition registries
     */
    List<FlowDefinitionRegistry> getMultifactorAuthenticationFlowDefinitionRegistries();
}
