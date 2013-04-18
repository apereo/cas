package org.jasig.cas.support.oauth;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

public class CasClientDetailsService implements ClientDetailsService {
    
    @NotNull
    private ServicesManager servicesManager;

    public CasClientDetailsService(ServicesManager servicesManager) {
        super();
        this.servicesManager = servicesManager;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        
        BaseClientDetails details = new BaseClientDetails();
        
        Collection<String> authorizedGrantTypes = new ArrayList<String>();
        authorizedGrantTypes.add("authorization_code");
        
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
