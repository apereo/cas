/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.authentication.principal;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jasig.cas.authentication.handler.DefaultPasswordEncoder;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class OpenIdService extends AbstractWebApplicationService {

    protected static final Logger LOG = LoggerFactory.getLogger(OpenIdService.class);
    
    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = 5776500133123291301L;

    private static final String CONST_PARAM_SERVICE = "openid.return_to";
    
    private static final PasswordEncoder ENCODER = new DefaultPasswordEncoder("SHA1");

    private static final KeyGenerator keyGenerator;

    private String identity;
    
    private final SecretKey sharedSecret;

    private final String signature;
    
    static {
        try {
            keyGenerator = KeyGenerator.getInstance("HmacSHA1");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected OpenIdService(final String id, final String originalUrl,
        final String artifactId, final String openIdIdentity,
        final String signature) {
        super(id, originalUrl, artifactId, null);
        this.identity = openIdIdentity;
        this.signature = signature;
        this.sharedSecret = keyGenerator.generateKey();
    }
    
    protected String generateHash(final String value) {
        try {
            final Mac sha1 = Mac.getInstance("HmacSHA1");
            sha1.init(this.sharedSecret);
            return Base64.encodeBase64String(sha1.doFinal(value.getBytes()));
        } catch (final Exception e) {
            LOG.error(e.getMessage(),e);
            return Base64.encodeBase64String(ENCODER.encode(value).getBytes());
        }
    }

    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<String, String>();
        
        if (ticketId != null) {
            parameters.put("openid.mode", "id_res");
            parameters.put("openid.identity", this.identity);
            parameters.put("openid.assoc_handle", ticketId);
            parameters.put("openid.return_to", getOriginalUrl());
            parameters.put("openid.signed", "identity,return_to");
            parameters.put("openid.sig", generateHash(
                "identity=" + this.identity + ",return_to=" + getOriginalUrl()));
        } else {
            parameters.put("openid.mode", "cancel");
        }
        
        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }

    public boolean logOutOfService(final String sessionIdentifier) {
        return false;
    }

    public static OpenIdService createServiceFrom(
        final HttpServletRequest request) {
        final String service = request.getParameter(CONST_PARAM_SERVICE);
        final String openIdIdentity = request.getParameter("openid.identity");
        final String signature = request.getParameter("openid.sig");

        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }

        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter("openid.assoc_handle");

        return new OpenIdService(id, service, artifactId, openIdIdentity,
            signature);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((this.identity == null) ? 0 : this.identity.hashCode());
        result = prime * result
            + ((this.signature == null) ? 0 : this.signature.hashCode());
        return result;
    }

    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OpenIdService other = (OpenIdService) obj;
        if (this.identity == null) {
            if (other.identity != null)
                return false;
        } else if (!this.identity.equals(other.identity))
            return false;
        return true;
    }

    public String getIdentity() {
        return this.identity;
    }

    public String getSignature() {
        return this.signature != null ? this.signature : generateHash(
            "identity=" + this.identity + ",return_to=" + getOriginalUrl());
    }
}
