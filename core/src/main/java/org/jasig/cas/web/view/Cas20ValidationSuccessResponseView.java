/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.io.PrintWriter;

import java.util.Map;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlOptions;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;

import edu.yale.tp.cas.ServiceResponseDocument;
import edu.yale.tp.cas.ServiceResponseType;
import edu.yale.tp.cas.AuthenticationSuccessType;
import edu.yale.tp.cas.ProxiesType;

/**
 * <p>CAS 2.0 validation success response view, implemented in XMLBeans.</p>
 * 
 * @author Drew Mazurek
 * @version $Revision$ $Date$
 * @since 3.0.1
 */
public class Cas20ValidationSuccessResponseView extends
		AbstractCas20ResponseView implements View {

	// get our superclass's default xmlOptions object
	private final XmlOptions xmlOptions = super.getXmlOptions();
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public final void render(final Map model, 
			final HttpServletRequest request,
			final HttpServletResponse response) throws Exception {
		final Assertion assertion = (Assertion) model
			.get(WebConstants.ASSERTION);
		
		/*
		 * The list of authentications contains the authenticated username
		 * and all the proxies through which this validation has proceeded.
		 * The username is the last element in the list, and the most
		 * recently visited proxy is at the beginning of the list.
		 */
		final List authentications = assertion.getChainedAuthentications();
		final ServiceResponseDocument responseDoc =
			ServiceResponseDocument.Factory.newInstance(xmlOptions);
		final ServiceResponseType serviceResponse = 
			responseDoc.addNewServiceResponse();
		final AuthenticationSuccessType authSuccess = 
			serviceResponse.addNewAuthenticationSuccess();
		authSuccess.setUser(((Authentication)authentications
                        .get(authentications.size()-1)).getPrincipal().getId());
        
		// do we have a PGTIOU? if so, add it.
		final String pgtIou = (String)model.get(WebConstants.PGTIOU);
		if(StringUtils.hasText(pgtIou)) {
			authSuccess.setProxyGrantingTicket(pgtIou);
		}
		
		// is there a proxy chain? if so, add it.
		if(authentications.size() > 1) {
			final ProxiesType proxies = authSuccess.addNewProxies();
			// skip i=authentications.size()-1 -- username
			for(int i=0,n=authentications.size()-1;i<n;i++) {
				proxies.addProxy(((Authentication)authentications.get(i))
						.getPrincipal().getId());
			}
		}
		
		response.setContentType(super.getHttpContentType() + "; charset="
				+ super.getHttpCharset());
		final PrintWriter out = response.getWriter();
		responseDoc.save(out,xmlOptions);
	}
}
