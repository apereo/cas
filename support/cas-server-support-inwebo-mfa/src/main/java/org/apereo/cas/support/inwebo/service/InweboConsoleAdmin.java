package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.soap.generated.LoginSearch;
import org.apereo.cas.support.inwebo.service.soap.generated.LoginSearchResponse;
import org.apereo.cas.support.inwebo.service.soap.generated.LoginSearchResult;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * The Inwebo SOAP client for user management.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class InweboConsoleAdmin extends WebServiceGatewaySupport {

    private final CasConfigurationProperties casProperties;

    /**
     * Login search.
     *
     * @param login the login
     * @return the login search result
     */
    public LoginSearchResult loginSearch(final String login) {
        val loginSearch = new LoginSearch();
        loginSearch.setUserid(0);
        loginSearch.setServiceid(casProperties.getAuthn().getMfa().getInwebo().getServiceId());
        loginSearch.setLoginname(login);
        loginSearch.setExactmatch(1);
        loginSearch.setOffset(0);
        loginSearch.setNmax(1);
        loginSearch.setSort(0);
        return ((LoginSearchResponse) getWebServiceTemplate().marshalSendAndReceive(loginSearch)).getLoginSearchReturn();
    }
}
