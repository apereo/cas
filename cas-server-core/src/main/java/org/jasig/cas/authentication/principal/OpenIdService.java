/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.handler.DefaultPasswordEncoder;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.webflow.util.Base64;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class OpenIdService extends AbstractWebApplicationService {

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = 5776500133123291301L;

    private static final String CONST_PARAM_SERVICE = "openid.return_to";

    // TODO use the DH-SHA one algorithm instead of just SHA-1
    private static final PasswordEncoder ENCODER = new DefaultPasswordEncoder("SHA1");

    private static final Base64 base64 = new Base64();

    private String identity;

    private final String signature;

    protected OpenIdService(final String id, final String originalUrl,
        final String artifactId, final String openIdIdentity,
        final String signature) {
        super(id, originalUrl, artifactId);
        this.identity = openIdIdentity;
        this.signature = signature;
    }

    public String getRedirectUrl(final String ticketId) {
        final StringBuilder builder = new StringBuilder();
        
        if (ticketId != null) {
            final Map<String, String> parameters = new HashMap<String, String>();

    
            parameters.put("openid.mode", "id_res");
            parameters.put("openid.identity", this.identity);
            parameters.put("openid.assoc_handle", ticketId);
            parameters.put("openid.return_to", getOriginalUrl());
            parameters.put("openid.signed", "identity,return_to");
            parameters.put("openid.sig", base64.encodeToString(ENCODER.encode(
                "identity=" + this.identity + ",return_to=" + getOriginalUrl())
                .getBytes()));
    
            builder.append(getOriginalUrl());
            builder.append(getOriginalUrl().contains("?") ? "&" : "?");
    
            for (final Map.Entry<String, String> entry : parameters.entrySet()) {
                builder.append(entry.getKey());
                builder.append("=");
    
                try {
                    builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (final Exception e) {
                    builder.append(entry.getValue());
                }
                builder.append("&");
            }
        } else {
            builder.append(getOriginalUrl());
            builder.append(getOriginalUrl().contains("?") ? "&" : "?");
            builder.append("openid.mode=cancel");
        }
        
        return builder.toString();
    }

    public boolean logOutOfService(final String sessionIdentifier) {
        return false;
    }

    public static WebApplicationService createServiceFrom(
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

    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof OpenIdService)) {
            return false;
        }

        final OpenIdService service = (OpenIdService) object;

        return getIdentity().equals(service.getIdentity())
            && getSignature().equals(service.getSignature())
            && this.getOriginalUrl().equals(service.getOriginalUrl());
    }

    public String getIdentity() {
        return this.identity;
    }

    public String getSignature() {
        return this.signature != null ? this.signature : base64
            .encodeToString(ENCODER.encode(
                "identity=" + this.identity + ",return_to=" + getOriginalUrl())
                .getBytes());
    }
}
