/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.samples.clients.remote.jaxrpc;

import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
import org.jasig.cas.remoting.CasService;
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
        CasService casService = (CasService)this.beanFactory.getBean("casService");
        UsernamePasswordAuthenticationRequest authRequest = new UsernamePasswordAuthenticationRequest();

        authRequest.setUserName("test");
        authRequest.setPassword("test");
        
        String ticketGrantingTicketId = casService.getTicketGrantingTicket(authRequest);
        String serviceTicket = casService.getServiceTicket(ticketGrantingTicketId, "http://www.rutgers.edu");

        System.out.println(serviceTicket);
    }

    public static void main(String[] args) {
        ListableBeanFactory beanFactory = new ClassPathXmlApplicationContext("clientContext.xml");
        JaxRpcSOAPWebServiceClient client = new JaxRpcSOAPWebServiceClient(beanFactory);
        client.invokeCasService();
    }
}
