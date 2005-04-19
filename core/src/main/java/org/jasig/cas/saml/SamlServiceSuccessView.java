/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.saml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.web.support.WebConstants;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLStatement;
import org.opensaml.SAMLSubject;
import org.springframework.web.servlet.View;

/**
 * This Spring View object is connected to an instance of 
 * org.jasig.cas.web.ServiceValidateController. It will be passed
 * control after a successful ST validation request. It will be
 * passed a Model containing an Assertion (in the CAS, not the
 * SAML sense) containing a chain of Principal objects. Its 
 * responsibility is to turn this information from the valid 
 * TGT chain into a SAML Response.
 * 
 * This is an extension to the CAS 2.0 protocol standard.
 * 
 * @author Howard Gilbert
 *
 */
public class SamlServiceSuccessView implements View {
	
	// This should be a parameter filled in by the configuration XML
	private String issuer = "example.org";

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void render(Map model, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			// This block is just careful not to throw Exceptions over the
			// wall to the container. InternalError is handled locally and
			// reflects an environmental contract violation.
			InternalError internalError= new InternalError();
			
			Assertion assertion = (Assertion) model.get(WebConstants.ASSERTION);
			if (assertion==null) 
				throw internalError;
			
			List/*<Principal>*/ chainedPrincipals = 
				assertion.getChainedPrincipals();
			if (chainedPrincipals==null || chainedPrincipals.size()==0)
				throw internalError;
			
			// For the moment, lets just handle the non-proxy chain response.
			Principal principal = (Principal) chainedPrincipals.get(0);
			String username = principal.getId();
			
			// I have to find out what the right string is here.
			String authenticationMethod="urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
			Date authTime = new Date();
			
			// Now build the response from the bottom up.
			SAMLNameIdentifier nameId = new SAMLNameIdentifier(username,null,null);
			SAMLSubject authNSubject = new SAMLSubject(nameId,null,null,null);
			List samlAssertions = new ArrayList();
			SAMLStatement[] statements = {
					new SAMLAuthenticationStatement(authNSubject, authenticationMethod, authTime, request
					.getRemoteAddr(), null, null)};

			SAMLAssertion samlAssertion = new SAMLAssertion(issuer, 
					new Date(System.currentTimeMillis()), 
					new Date(System.currentTimeMillis() + 300000), 
					null, null, Arrays.asList(statements));
			samlAssertions.add(samlAssertion);
			
			// I think this should be the URL of the service validating the Ticket
			// In reality, I have to get it from the Model, but currently its not
			// there so we put in a dummy.
			String serviceUrl = "service.example.org";
			
			SAMLResponse samlResponse = new SAMLResponse(null, serviceUrl, samlAssertions, null);
			
			response.setContentType("text/xml");
			String serializedSaml = samlResponse.toString();
			response.getWriter().print(serializedSaml);
		} catch (InternalError ie) {
			response.setStatus(500);
			return;
		} catch (Exception e) {
			response.setStatus(500);
			return;
		}

	}
	
	private class InternalError extends Exception {}

	/**
	 * @return Returns the issuer.
	 */
	public String getIssuer() {
		return issuer;
	}
	/**
	 * @param issuer The issuer to set.
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
}
