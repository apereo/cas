/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.samples.clients.remote.jaxrpc;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Assertion;
import org.jasig.cas.authentication.AuthenticationSpecification;
import org.jasig.cas.authentication.Cas10ProtocolAuthenticationSpecification;
import org.jasig.cas.authentication.SimpleService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class JaxRpcSOAPWebServiceClient {

    private final BeanFactory beanFactory;

    public JaxRpcSOAPWebServiceClient(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void invokeCasService() {
        CentralAuthenticationService centralAuthenticationService = (CentralAuthenticationService)this.beanFactory.getBean("casService");
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        AuthenticationSpecification authenticationSpecification = new Cas10ProtocolAuthenticationSpecification(false);

        authRequest.setUserName("test");
        authRequest.setPassword("test");
        try {
            String ticketGrantingTicketId = centralAuthenticationService.createTicketGrantingTicket(authRequest);
            String serviceTicket = centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId,
                new SimpleService("http://www.rutgers.edu"));
            Assertion assertion = centralAuthenticationService.validateServiceTicket(serviceTicket, new SimpleService("http://www.rutgers.edu"));

            if (!authenticationSpecification.isSatisfiedBy(assertion)) {
                throw new TicketException(TicketException.INVALID_TICKET, "ticket not backed by initial CAS login, as requested");
            }

            System.out.println(serviceTicket);
            System.out.println(((Principal)assertion.getChainedPrincipals().get(0)).getId());
        }
        catch (TicketException tce) {
            System.out.println("Error getting Ticket:" + tce);
        }
        catch (org.jasig.cas.authentication.AuthenticationException ae) {
            System.out.println("Error authenticating: " + ae);
        }
    }

    public static void main(String[] args) {
        ListableBeanFactory beanFactory = new ClassPathXmlApplicationContext("clientContext.xml");
        JaxRpcSOAPWebServiceClient client = new JaxRpcSOAPWebServiceClient(beanFactory);
        client.invokeCasService();
    }
}
