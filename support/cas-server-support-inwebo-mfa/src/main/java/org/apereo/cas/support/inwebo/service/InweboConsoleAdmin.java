package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.support.inwebo.service.soap.LoginSearch;
import org.apereo.cas.support.inwebo.service.soap.LoginSearchResponse;
import org.apereo.cas.support.inwebo.service.soap.LoginSearchResult;

import lombok.val;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * The Inwebo SOAP client for user management.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public class InweboConsoleAdmin extends WebServiceGatewaySupport {

    public LoginSearchResult loginSearch(final String login, final long serviceId) {
        val loginSearch = new LoginSearch();
        loginSearch.setUserid(0);
        loginSearch.setServiceid(serviceId);
        loginSearch.setLoginname(login);
        loginSearch.setExactmatch(1);
        loginSearch.setOffset(0);
        loginSearch.setNmax(1);
        loginSearch.setSort(0);
        return ((LoginSearchResponse) getWebServiceTemplate().marshalSendAndReceive(loginSearch)).getLoginSearchReturn();
    }
}
