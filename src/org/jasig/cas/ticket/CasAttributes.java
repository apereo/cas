/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class CasAttributes {

    // LOGIN TICKET ???
    // TICKET???
    private String service;

    private boolean renew;

    private String gateway;

    private boolean warn;

    private boolean first;

    private String pgtIou;

    private String pgtUrl;

    private String callbackUrl;

    private String targetService;

    /**
     * @return Returns the callbackUrl.
     */
    public String getCallbackUrl() {
        return this.callbackUrl;
    }

    /**
     * @return Returns the targetService.
     */
    public String getTargetService() {
        return this.targetService;
    }

    /**
     * @param targetService The targetService to set.
     */
    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    /**
     * @param callbackUrl The callbackUrl to set.
     */
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    /**
     * @return Returns the first.
     */
    public boolean isFirst() {
        return this.first;
    }

    /**
     * @param first The first to set.
     */
    public void setFirst(boolean first) {
        this.first = first;
    }

    /**
     * @return Returns the gateway.
     */
    public String getGateway() {
        return this.gateway;
    }

    /**
     * @param gateway The gateway to set.
     */
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    /**
     * @return Returns the pgtIou.
     */
    public String getPgtIou() {
        return this.pgtIou;
    }

    /**
     * @param pgtIou The pgtIou to set.
     */
    public void setPgtIou(String pgtIou) {
        this.pgtIou = pgtIou;
    }

    /**
     * @return Returns the pgtUrl.
     */
    public String getPgtUrl() {
        return this.pgtUrl;
    }

    /**
     * @param pgtUrl The pgtUrl to set.
     */
    public void setPgtUrl(String pgtUrl) {
        this.pgtUrl = pgtUrl;
    }

    /**
     * @return Returns the renew.
     */
    public boolean isRenew() {
        return this.renew;
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
        return this.service;
    }

    /**
     * @param service The service to set.
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return Returns the warn.
     */
    public boolean isWarn() {
        return this.warn;
    }

    /**
     * @param warn The warn to set.
     */
    public void setWarn(boolean warn) {
        this.warn = warn;
    }
}
