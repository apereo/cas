package org.jasig.cas.support.oauth;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

public class CasClientDetailsService implements ClientDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(CasClientDetailsService.class);
    
    @NotNull
    private ServicesManager servicesManager;
    
    public CasClientDetailsService(ServicesManager servicesManager) {
        super();
        this.servicesManager = servicesManager;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        
        log.debug("Called loadClientByClientId with argument {}", clientId);
        
        BaseClientDetails details = new BaseClientDetails();
        
        // By default, only use secure grant types for every client
        Collection<String> authorizedGrantTypes = new ArrayList<String>();
        authorizedGrantTypes.add(GrantType.AUTHORIZATION_CODE);
        authorizedGrantTypes.add(GrantType.PASSWORD);
        authorizedGrantTypes.add(GrantType.REFRESH_TOKEN);
        
        // Set the client id and secret based on the service name and service description respectively
        for (RegisteredService service: servicesManager.getAllServices()) {
            if (clientId.equals(service.getName())) {
                details.setClientId(clientId);
                details.setClientSecret(service.getDescription());
                details.setAuthorizedGrantTypes(authorizedGrantTypes);
                break;
            }
        }
        
        return details;
    }

}
