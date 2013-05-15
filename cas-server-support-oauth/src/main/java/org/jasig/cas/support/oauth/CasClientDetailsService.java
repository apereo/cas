package org.jasig.cas.support.oauth;

import java.util.Collection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

/**
 * An implementation that looks for client information in the injected CAS services manager. Follows the convention:
 * client_id = service name
 * client_secret = service description
 *
 * Note that CAS as of yet does not have a concept of a client defined in the OAuth sense, hence the need for a
 * definition in the services manager. This has the caveat of not being able to restrict access based on *which*
 * client is used, only that an authorized user is providing credentials for a valid client.
 *
 * @author Joe McCall
 */
public class CasClientDetailsService implements ClientDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CasClientDetailsService.class);

    @NotNull
    @Size(min = 1)
    private Collection<String> authorizedGrantTypes;

    @NotNull
    private ServicesManager servicesManager;

    public CasClientDetailsService(final ServicesManager servicesManager, final Collection<String> authorizedGrantTypes) {
        super();
        this.servicesManager = servicesManager;
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    @Override
    public ClientDetails loadClientByClientId(final String clientId) {

        log.debug("Called loadClientByClientId with argument {}", clientId);

        BaseClientDetails details = null;

        // Set the client id and secret based on the service name and service
        // description respectively
        for (RegisteredService service: servicesManager.getAllServices()) {
            if (clientId.equals(service.getName())) {
                details = new BaseClientDetails();
                details.setClientId(clientId);
                details.setClientSecret(service.getDescription());
                details.setAuthorizedGrantTypes(authorizedGrantTypes);
                break;
            }
        }

        if (details == null) {
            throw new ClientRegistrationException("Client not found with clientId " + clientId);
        }

        return details;
    }

}
