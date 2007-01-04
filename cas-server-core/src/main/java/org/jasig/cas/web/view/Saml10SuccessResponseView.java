/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.AttributePrincipal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.web.support.SamlArgumentExtractor;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAttribute;
import org.opensaml.SAMLAttributeStatement;
import org.opensaml.SAMLAudienceRestrictionCondition;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLSubject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Implementation of a view to return a SAML response and assertion, based on
 * the SAML 1.1 specification.
 * <p>
 * If an AttributePrincipal is supplied, then the assertion will include the
 * attributes from it (assuming a String key/Object value pair). The only
 * Authentication attribute it will look at is the authMethod (if supplied).
 * <p>
 * Note that this class will currently not handle proxy authentication.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Saml10SuccessResponseView extends AbstractCasView implements
    InitializingBean {

    /** Namespace for custom attributes. */
    private static final String NAMESPACE = "http://www.ja-sig.org/cas/";

    /** The issuer, generally the hostname. */
    private String issuer;

    /** The amount of time in milliseconds this is valid for. */
    private long issueLength = 30000;

    /** Instance of strategy for extracting arguments. */
    private final SamlArgumentExtractor samlArgumentExtractor = new SamlArgumentExtractor();

    protected void renderMergedOutputModel(final Map model,
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {

        final Assertion assertion = getAssertionFrom(model);
        final Authentication authentication = assertion
            .getChainedAuthentications()[0];
        final Date currentDate = new Date();
        final String authenticationMethod = (String) authentication
            .getAttributes().get("samlAuthenticationStatement::authMethod");
        final Service service = this.samlArgumentExtractor
            .extractService(request);

        final SAMLResponse samlResponse = new SAMLResponse(
            null, service
                .getId(), new ArrayList(), null);

        samlResponse.setIssueInstant(currentDate);

        final SAMLAssertion samlAssertion = new SAMLAssertion();
        samlAssertion.setIssueInstant(currentDate);
        samlAssertion.setIssuer(this.issuer);
        samlAssertion.setNotBefore(currentDate);
        samlAssertion.setNotOnOrAfter(new Date(currentDate.getTime()
            + this.issueLength));

        final SAMLAudienceRestrictionCondition samlAudienceRestrictionCondition = new SAMLAudienceRestrictionCondition();
        samlAudienceRestrictionCondition.addAudience(service.getId());

        final SAMLAuthenticationStatement samlAuthenticationStatement = new SAMLAuthenticationStatement();
        samlAuthenticationStatement.setAuthInstant(authentication
            .getAuthenticatedDate());
        samlAuthenticationStatement.setAuthMethod(authenticationMethod != null
            ? authenticationMethod
            : SAMLAuthenticationStatement.AuthenticationMethod_Unspecified);

        samlAuthenticationStatement.setSubject(getSamlSubject(authentication));

        if (authentication.getPrincipal() instanceof AttributePrincipal) {
            final AttributePrincipal attributePrincipal = (AttributePrincipal) authentication
                .getPrincipal();
            final SAMLAttributeStatement attributeStatement = new SAMLAttributeStatement();

            attributeStatement.setSubject(getSamlSubject(authentication));
            samlAssertion.addStatement(attributeStatement);

            for (final Iterator iter = attributePrincipal.getAttributes()
                .keySet().iterator(); iter.hasNext();) {

                final Object key = iter.next();
                final Object value = attributePrincipal.getAttributes()
                    .get(key);

                final SAMLAttribute attribute = new SAMLAttribute();
                attribute.setName((String) key);
                attribute.setNamespace(NAMESPACE);

                if (value instanceof Collection) {
                    attribute.setValues((Collection) value);
                } else {
                    final Collection c = new ArrayList();
                    c.add(value);
                    attribute.setValues(c);
                }

                attributeStatement.addAttribute(attribute);
            }
        }

        samlAssertion.addStatement(samlAuthenticationStatement);
        samlAssertion.addCondition(samlAudienceRestrictionCondition);
        samlResponse.addAssertion(samlAssertion);

        final String xmlResponse = samlResponse.toString();

        response.getWriter()
            .print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        response.setContentType("text/xml");
        response.getWriter().print(xmlResponse);
        response.flushBuffer();
    }

    protected SAMLSubject getSamlSubject(final Authentication authentication)
        throws SAMLException {
        final SAMLSubject samlSubject = new SAMLSubject();
        samlSubject.addConfirmationMethod(SAMLSubject.CONF_ARTIFACT);
        final SAMLNameIdentifier samlNameIdentifier = new SAMLNameIdentifier();
        samlNameIdentifier.setName(authentication.getPrincipal().getId());

        samlSubject.setNameIdentifier(samlNameIdentifier);

        return samlSubject;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.issuer, "issuer cannot be null.");
    }

    public void setIssueLength(final long issueLength) {
        this.issueLength = issueLength;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }
}
