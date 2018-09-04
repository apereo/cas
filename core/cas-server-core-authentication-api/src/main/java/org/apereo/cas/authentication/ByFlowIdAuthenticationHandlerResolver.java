package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.Optional;
import java.util.Set;

@Slf4j
public class ByFlowIdAuthenticationHandlerResolver extends ByCredentialTypeAuthenticationHandlerResolver {

    public ByFlowIdAuthenticationHandlerResolver(final Class<? extends Credential>... creds){
        super(creds);
    }



    @Override
    public Set<AuthenticationHandler> resolve(Set<AuthenticationHandler> candidateHandlers, AuthenticationTransaction transaction) {
        final String flowId = RequestContextHolder.getRequestContext().getActiveFlow().getId();
        Optional<AuthenticationHandler> resolved = candidateHandlers.stream()
        .filter(handler -> handler.getName().equals(flowId)).findFirst();

        if (!resolved.isPresent()) {
            LOGGER.error("Authentication handler could not be resolved");
            return null;
        }
        final AuthenticationHandler authHandler = resolved.get();
        LOGGER.debug("Resolved Auth Handler is [{}]", authHandler.getName());

        return CollectionUtils.wrapSet(authHandler);
    }

}
