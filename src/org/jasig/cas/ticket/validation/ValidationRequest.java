/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Object to hold all of the basic attributes required to create a ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ValidationRequest {
	private String ticket;
	private String pgtIou;
	private String pgtUrl;
	private String callbackUrl;
	private String service;
	private String pgt;
	private String targetService;
	private boolean renew;
	private Principal principal;

	/**
	 * @return Returns the principal.
	 */
	public Principal getPrincipal() {
		return principal;
	}
	/**
	 * @param principal The principal to set.
	 */
	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}
	/**
	 * @return Returns the pgt.
	 */
	public String getPgt()
	{
		return pgt;
	}
	/**
	 * @return Returns the renew.
	 */
	public boolean isRenew() {
		return renew;
	}
	/**
	 * @param renew The renew to set.
	 */
	public void setRenew(boolean renew) {
		this.renew = renew;
	}
	/**
	 * @return Returns the service.
	 */
	public String getService() {
		return service;
	}
	/**
	 * @param service The service to set.
	 */
	public void setService(String service) {
		this.service = service;
	}
	/**
	 * @param pgt The pgt to set.
	 */
	public void setPgt(final String pgt)
	{
		this.pgt = pgt;
	}
	
	/**
	 * @return Returns the ticket.
	 */
	public String getTicket() {
		return ticket;
	}
	/**
	 * @param ticket The ticket to set.
	 */
	public void setTicket(final String ticket) {
		this.ticket = ticket;
	}

	/**
	 * @return Returns the pgtIou.
	 */
	public String getPgtIou() {
		return pgtIou;
	}
	/**
	 * @param pgtIou The pgtIou to set.
	 */
	public void setPgtIou(final String pgtIou) {
		this.pgtIou = pgtIou;
	}
	/**
	 * @return Returns the callbackUrl.
	 */
	public String getCallbackUrl() {
		return callbackUrl;
	}
	/**
	 * @param callbackUrl The callbackUrl to set.
	 */
	public void setCallbackUrl(final String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	/**
	 * @return Returns the pgtUrl.
	 */
	public String getPgtUrl() {
		return pgtUrl;
	}
	/**
	 * @param pgtUrl The pgtUrl to set.
	 */
	public void setPgtUrl(final String pgtUrl) {
		this.pgtUrl = pgtUrl;
	}
	/**
	 * @return Returns the targetService.
	 */
	public String getTargetService()
	{
		return targetService;
	}
	/**
	 * @param targetService The targetService to set.
	 */
	public void setTargetService(final String targetService)
	{
		this.targetService = targetService;
	}
}
