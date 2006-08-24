/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;

/**
 * Handles all retrieval and placement of attributes in the request scope for
 * both HTTP requests and Flow.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class CasArgumentExtractor {

    private static final String DEFAULT_PGT_URL_PARAMETER_NAME = "pgtUrl";

    private static final String DEFAULT_RENEW_PARAMETER_NAME = "renew";

    private static final String DEFAULT_GATEWAY_PARAMETER_NAME = "gateway";

    private static final String DEFAULT_TICKET_PARAMETER_NAME = "ticket";

    private static final String DEFAULT_SERVICE_PARAMETER_NAME = "service";

    private static final String DEFAULT_WARN_PARAMETER_NAME = "warn";

    private static final String DEFAULT_PROXY_GRANTING_TICKET_PARAMETER_NAME = "pgt";

    private static final String DEFAULT_TARGET_SERVICE_PARAMETER_NAME = "targetService";

    private String targetServiceParameterName = DEFAULT_TARGET_SERVICE_PARAMETER_NAME;

    private String renewParameterName = DEFAULT_RENEW_PARAMETER_NAME;

    private String gatewayParameterName = DEFAULT_GATEWAY_PARAMETER_NAME;

    private String ticketParameterName = DEFAULT_TICKET_PARAMETER_NAME;

    private String serviceParameterName = DEFAULT_SERVICE_PARAMETER_NAME;

    private String warnParameterName = DEFAULT_WARN_PARAMETER_NAME;

    private String pgtUrlParameterName = DEFAULT_PGT_URL_PARAMETER_NAME;

    private String proxyGrantingTicketParameterName = DEFAULT_PROXY_GRANTING_TICKET_PARAMETER_NAME;

    private CookieGenerator warnCookieGenerator;

    private CookieGenerator ticketGrantingTicketCookieGenerator;

    /**
     * Constructor that accepts two CookieGenerators for generating
     * TicketGrantingTicket cookies and Warn Cookies.
     * 
     * @param ticketGrantingTicketCookieGenerator the cookie generator to use
     * when generating TGT Cookies.
     * @param warnCookieGenerator the cookie generator to use when generating
     * Warn cookies.
     */
    public CasArgumentExtractor(
        final CookieGenerator ticketGrantingTicketCookieGenerator,
        final CookieGenerator warnCookieGenerator) {
        Assert.notNull(ticketGrantingTicketCookieGenerator,
            "ticketGrantingTicketGenerator is required.");
        Assert.notNull(warnCookieGenerator, "warnCookieGenerator is required.");
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.warnCookieGenerator = warnCookieGenerator;
    }

    /**
     * According to the CAS 2.0 Protocol, the presence of the renew parameter
     * indicates whether renew is set to true (not its acctual value)
     * 
     * @param request the HttpServletRequest
     * @return true if renew is set, false otherwise.
     */
    public boolean isRenewPresent(final HttpServletRequest request) {
        return StringUtils.hasText(request
            .getParameter(this.renewParameterName));
    }

    public boolean isRenewPresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get(this.renewParameterName));
    }

    public String getRenewParameterName() {
        return this.renewParameterName;
    }

    public boolean isGatewayPresent(final HttpServletRequest request) {
        return StringUtils.hasText(request
            .getParameter(this.gatewayParameterName));
    }

    public boolean isGatewayPresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get(this.gatewayParameterName));
    }

    public void setTicketParameterName(final String ticketParameterName) {
        Assert.notNull(ticketParameterName);
        this.ticketParameterName = ticketParameterName;
    }

    public String getTicketParameterName() {
        return this.ticketParameterName;
    }

    public String extractTicketFrom(final HttpServletRequest request) {
        return request.getParameter(this.ticketParameterName);
    }

    public String extractTicketFrom(final RequestContext context) {
        return context.getExternalContext().getRequestParameterMap().get(
            this.ticketParameterName);
    }

    public boolean isTicketPresent(final HttpServletRequest request) {
        return StringUtils.hasText(request
            .getParameter(this.ticketParameterName));
    }

    public boolean isTicketPresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get(this.ticketParameterName));
    }

    /** Note this automatically cleans up jsessions */
    public Service extractServiceFrom(final HttpServletRequest request) {
        if (!isServicePresent(request)) {
            return null;
        }

        return new SimpleService(stripJsessionFromUrl(request
            .getParameter(this.serviceParameterName)));
    }

    /** Note this automatically cleans up jsessions */
    public Service extractServiceFrom(final RequestContext context) {
        if (!isServicePresent(context)) {
            return null;
        }

        return new SimpleService(stripJsessionFromUrl(context
            .getExternalContext().getRequestParameterMap().get(
                this.serviceParameterName)));
    }

    public boolean isServicePresent(final HttpServletRequest request) {
        return StringUtils.hasText(request
            .getParameter(this.serviceParameterName));
    }

    public boolean isServicePresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get(this.serviceParameterName));
    }

    public String getServiceParameterName() {
        return this.serviceParameterName;
    }

    public boolean isWarnPresent(final HttpServletRequest request) {
        return StringUtils
            .hasText(request.getParameter(this.warnParameterName));
    }

    public String getWarnParameterName() {
        return this.warnParameterName;
    }

    public boolean isWarnPresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get(this.warnParameterName));
    }

    public boolean isWarnCookiePresent(final RequestContext context) {
        final HttpServletRequest request = getHttpServletRequest(context);
        final Cookie cookie = WebUtils.getCookie(request,
            this.warnCookieGenerator.getCookieName());

        if (cookie == null) {
            return false;
        }

        return Boolean.valueOf(cookie.getValue()).booleanValue();
    }

    public void putWarnCookieIfRequestParameterPresent(
        final RequestContext context) {
        final HttpServletResponse response = getHttpServletResponse(context);

        if (isWarnPresent(context)) {
            this.warnCookieGenerator.addCookie(response, "true");
        } else {
            this.warnCookieGenerator.removeCookie(response);
        }
    }

    public void putTicketGrantingTicketIn(final RequestContext context,
        final String value) {
        context.getRequestScope().put("ticketGrantingTicketId", value);
    }

    public String getTicketGrantingTicketFrom(final RequestContext context) {
        return context.getRequestScope().getString("ticketGrantingTicketId");
    }

    public void putServiceTicketIn(final RequestContext context,
        final String value) {
        context.getRequestScope().put("serviceTicketId", value);
    }

    public String getServiceTicketFrom(final RequestContext context) {
        return context.getRequestScope().getString("serviceTicketId");
    }

    public Service extractTargetService(final HttpServletRequest request) {
        if (!isTargetServicePresent(request)) {
            return null;
        }

        return new SimpleService(request
            .getParameter(this.targetServiceParameterName));
    }

    public boolean isTargetServicePresent(final HttpServletRequest request) {
        return StringUtils.hasText(request
            .getParameter(this.targetServiceParameterName));
    }

    public String getTargetServiceParameterName() {
        return this.targetServiceParameterName;
    }

    public boolean isTicketGrantingTicketCookiePresent(
        final RequestContext context) {
        final HttpServletRequest request = getHttpServletRequest(context);
        return WebUtils.getCookie(request,
            this.ticketGrantingTicketCookieGenerator.getCookieName()) != null;
    }

    public String extractTicketGrantingTicketFromCookie(
        final RequestContext context) {
        if (!isTicketGrantingTicketCookiePresent(context)) {
            return null;
        }

        final HttpServletRequest request = getHttpServletRequest(context);
        return WebUtils.getCookie(request,
            this.ticketGrantingTicketCookieGenerator.getCookieName())
            .getValue();
    }

    public void putTicketGrantingTicketInCookie(final RequestContext context,
        final String value) {
        final HttpServletResponse response = getHttpServletResponse(context);
        this.ticketGrantingTicketCookieGenerator.addCookie(response, value);
    }

    public String extractTicketGrantingTicketFromCookie(
        final HttpServletRequest request) {
        final Cookie cookie = WebUtils.getCookie(request,
            this.ticketGrantingTicketCookieGenerator.getCookieName());
        if (cookie == null) {
            return null;
        }

        return cookie.getValue();
    }

    public String extractProxyGrantingTicketCallbackUrl(
        final HttpServletRequest request) {
        return request.getParameter(this.pgtUrlParameterName);
    }

    public String getProxyGrantingTicketCallbackUrlParameterName() {
        return this.pgtUrlParameterName;
    }

    public String extractProxyGrantingTicket(final HttpServletRequest request) {
        return request.getParameter(this.proxyGrantingTicketParameterName);
    }

    public String getProxyGrantingTicketParameterName() {
        return this.proxyGrantingTicketParameterName;
    }

    public final HttpServletRequest getHttpServletRequest(
        final RequestContext context) {
        if (context.getExternalContext().getClass().equals(
            ServletExternalContext.class)) {
            return ((ServletExternalContext) context.getExternalContext())
                .getRequest();
        }

        throw new IllegalStateException(
            "Cannot obtain HttpServletRequest from event of type: "
                + context.getExternalContext().getClass().getName());
    }

    protected final HttpServletResponse getHttpServletResponse(
        final RequestContext context) {
        if (context.getExternalContext().getClass().equals(
            ServletExternalContext.class)) {
            return ((ServletExternalContext) context.getExternalContext())
                .getResponse();
        }

        throw new IllegalStateException(
            "Cannot obtain HttpServletResponse from event of type: "
                + context.getExternalContext().getClass().getName());
    }

    protected final String stripJsessionFromUrl(final String url) {
        final int jsessionPosition = url.indexOf(";jsession");

        if (jsessionPosition == -1) {
            return url;
        }

        final int questionMarkPosition = url.indexOf("?");

        if (questionMarkPosition < jsessionPosition) {
            return url.substring(0, url.indexOf(";jsession"));
        }

        return url.substring(0, jsessionPosition)
            + url.substring(questionMarkPosition);
    }

}
