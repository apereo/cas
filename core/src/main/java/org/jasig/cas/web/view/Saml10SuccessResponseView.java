/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.web.CasArgumentExtractor;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAudienceRestrictionCondition;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLSubject;


public class Saml10SuccessResponseView extends AbstractCasView {
    
    private String issuer;
    
    private long issueLength;
    
    private CasArgumentExtractor casArgumentExtractor;

    protected void renderMergedOutputModel(final Map model, final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        
        final Assertion assertion = getAssertionFrom(model);
        final Authentication authentication = assertion.getChainedAuthentications()[0];
        final Date currentDate = new Date();
        final String authenticationMethod = (String) authentication.getAttributes().get("samlAuthenticationStatement::authMethod");
        final Service service = this.casArgumentExtractor.extractServiceFrom(request);
        
        final SAMLResponse samlResponse = new SAMLResponse();
        samlResponse.setRecipient(service.getId());
        samlResponse.setIssueInstant(currentDate);
        samlResponse.setInResponseTo(this.casArgumentExtractor.extractTicketFrom(request));
        
        final SAMLAssertion samlAssertion = new SAMLAssertion();
        samlAssertion.setIssueInstant(currentDate);
        samlAssertion.setIssuer(this.issuer);
        samlAssertion.setNotBefore(currentDate);
        samlAssertion.setNotOnOrAfter(new Date(currentDate.getTime() + this.issueLength));
        
        final SAMLAudienceRestrictionCondition samlAudienceRestrictionCondition = new SAMLAudienceRestrictionCondition();
        samlAudienceRestrictionCondition.addAudience(service.getId());
        
        final SAMLAuthenticationStatement samlAuthenticationStatement = new SAMLAuthenticationStatement();
        samlAuthenticationStatement.setAuthInstant(authentication.getAuthenticatedDate());
        samlAuthenticationStatement.setAuthMethod(authenticationMethod != null ? authenticationMethod : SAMLAuthenticationStatement.AuthenticationMethod_Unspecified);
        
        final SAMLSubject samlSubject = new SAMLSubject();
        
        final SAMLNameIdentifier samlNameIdentifier = new SAMLNameIdentifier();
        samlNameIdentifier.setName(authentication.getPrincipal().getId());
        
        samlSubject.setNameIdentifier(samlNameIdentifier);
        samlAuthenticationStatement.setSubject(samlSubject);
        samlAssertion.addStatement(samlAuthenticationStatement);
        samlAssertion.addCondition(samlAudienceRestrictionCondition);
        samlResponse.addAssertion(samlAssertion);
        
        final String xmlResponse = samlResponse.toString();
        
        response.getWriter().print(xmlResponse);
    }
    
    public void setCasArgumentExtractor(final CasArgumentExtractor casArgumentExtractor) {
        this.casArgumentExtractor = casArgumentExtractor;
    }

}
