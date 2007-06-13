/* $$ This file has been instrumented by Clover 1.3.12#20060208202937157 $$ *//*
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.DefaultPasswordEncoder;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.Response;
import org.springframework.util.StringUtils;
import org.springframework.webflow.util.Base64;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class OpenIdService extends AbstractWebApplicationService {public static class __CLOVER_4_0{public static com_cenqua_clover.g cloverRec;static{try{if(20060208202937157L!=com_cenqua_clover.CloverVersionInfo.getBuildMagic()){java.lang.System.err.println("[CLOVER] WARNING: The Clover version used in instrumentation does not match the runtime version. You need to run instrumented classes against the same version of Clover that you instrumented with.");java.lang.System.err.println("[CLOVER] WARNING: Instr=1.3.12#20060208202937157,Runtime="+com_cenqua_clover.CloverVersionInfo.getReleaseNum() + "#"+com_cenqua_clover.CloverVersionInfo.getBuildMagic());}cloverRec = com_cenqua_clover.f.getRecorder(new char[] {67,58,92,119,111,114,107,115,112,97,99,101,92,99,97,115,51,92,99,97,115,45,115,101,114,118,101,114,45,115,117,112,112,111,114,116,45,111,112,101,110,105,100,92,116,97,114,103,101,116,47,99,108,111,118,101,114,47,99,108,111,118,101,114,46,100,98},1181745647584L,500, true);}catch (Throwable t) {java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised. Are you sure you have Clover in the runtime classpath? ("+t.getClass()+":"+ t.getMessage()+")");}}}

    protected static final Log LOG = LogFactory.getLog(OpenIdService.class);
    
    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = 5776500133123291301L;

    private static final String CONST_PARAM_SERVICE = "openid.return_to";
    
    private static final PasswordEncoder ENCODER = new DefaultPasswordEncoder("SHA1");

    private static final Base64 base64 = new Base64();
    
    private static final KeyGenerator keyGenerator;

    private String identity;
    
    private final SecretKey sharedSecret;

    private final String signature;
    
    static {
        __CLOVER_4_0.cloverRec.S[416]++;try {
            __CLOVER_4_0.cloverRec.S[417]++;keyGenerator = KeyGenerator.getInstance("HmacSHA1");
        } catch (final NoSuchAlgorithmException e) {
            __CLOVER_4_0.cloverRec.S[418]++;throw new RuntimeException(e);
        }
    }

    protected OpenIdService(final String id, final String originalUrl,
        final String artifactId, final String openIdIdentity,
        final String signature) {
        super(id, originalUrl, artifactId);__CLOVER_4_0.cloverRec.S[419]++;try {__CLOVER_4_0.cloverRec.M[111]++;
        __CLOVER_4_0.cloverRec.S[420]++;this.identity = openIdIdentity;
        __CLOVER_4_0.cloverRec.S[421]++;this.signature = signature;
        __CLOVER_4_0.cloverRec.S[422]++;this.sharedSecret = keyGenerator.generateKey();
    }finally{__CLOVER_4_0.cloverRec.D = true;}}
    
    protected String generateHash(final String value) {try {__CLOVER_4_0.cloverRec.M[112]++;
        __CLOVER_4_0.cloverRec.S[423]++;try {
            __CLOVER_4_0.cloverRec.S[424]++;final Mac sha1 = Mac.getInstance("HmacSHA1");
            __CLOVER_4_0.cloverRec.S[425]++;sha1.init(this.sharedSecret);
            __CLOVER_4_0.cloverRec.S[426]++;return base64.encodeToString(sha1.doFinal(value.getBytes()));
        } catch (final Exception e) {
            __CLOVER_4_0.cloverRec.S[427]++;LOG.error(e,e);
            __CLOVER_4_0.cloverRec.S[428]++;return base64.encodeToString(ENCODER.encode(value).getBytes());
        }
    }finally{__CLOVER_4_0.cloverRec.D = true;}}

    public Response getResponse(final String ticketId) {try {__CLOVER_4_0.cloverRec.M[113]++;
        __CLOVER_4_0.cloverRec.S[429]++;final Map<String, String> parameters = new HashMap<String, String>();
        
        __CLOVER_4_0.cloverRec.S[430]++;if ((((ticketId != null) && (++__CLOVER_4_0.cloverRec.CT[66]!=0|true)) || (++__CLOVER_4_0.cloverRec.CF[66]==0&false))) {{
            __CLOVER_4_0.cloverRec.S[431]++;parameters.put("openid.mode", "id_res");
            __CLOVER_4_0.cloverRec.S[432]++;parameters.put("openid.identity", this.identity);
            __CLOVER_4_0.cloverRec.S[433]++;parameters.put("openid.assoc_handle", ticketId);
            __CLOVER_4_0.cloverRec.S[434]++;parameters.put("openid.return_to", getOriginalUrl());
            __CLOVER_4_0.cloverRec.S[435]++;parameters.put("openid.signed", "identity,return_to");
            __CLOVER_4_0.cloverRec.S[436]++;parameters.put("openid.sig", generateHash(
                "identity=" + this.identity + ",return_to=" + getOriginalUrl()));
        } }else {{
            __CLOVER_4_0.cloverRec.S[437]++;parameters.put("openid.mode", "cancel");
        }
        
        }__CLOVER_4_0.cloverRec.S[438]++;return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }finally{__CLOVER_4_0.cloverRec.D = true;}}

    public boolean logOutOfService(final String sessionIdentifier) {try {__CLOVER_4_0.cloverRec.M[114]++;
        __CLOVER_4_0.cloverRec.S[439]++;return false;
    }finally{__CLOVER_4_0.cloverRec.D = true;}}

    public static OpenIdService createServiceFrom(
        final HttpServletRequest request) {try {__CLOVER_4_0.cloverRec.M[115]++;
        __CLOVER_4_0.cloverRec.S[440]++;final String service = request.getParameter(CONST_PARAM_SERVICE);
        __CLOVER_4_0.cloverRec.S[441]++;final String openIdIdentity = request.getParameter("openid.identity");
        __CLOVER_4_0.cloverRec.S[442]++;final String signature = request.getParameter("openid.sig");

        __CLOVER_4_0.cloverRec.S[443]++;if ((((openIdIdentity == null || !StringUtils.hasText(service)) && (++__CLOVER_4_0.cloverRec.CT[67]!=0|true)) || (++__CLOVER_4_0.cloverRec.CF[67]==0&false))) {{
            __CLOVER_4_0.cloverRec.S[444]++;return null;
        }

        }__CLOVER_4_0.cloverRec.S[445]++;final String id = cleanupUrl(service);
        __CLOVER_4_0.cloverRec.S[446]++;final String artifactId = request.getParameter("openid.assoc_handle");

        __CLOVER_4_0.cloverRec.S[447]++;return new OpenIdService(id, service, artifactId, openIdIdentity,
            signature);
    }finally{__CLOVER_4_0.cloverRec.D = true;}}

    public boolean equals(final Object object) {try {__CLOVER_4_0.cloverRec.M[116]++;
        __CLOVER_4_0.cloverRec.S[448]++;if ((((object == null) && (++__CLOVER_4_0.cloverRec.CT[68]!=0|true)) || (++__CLOVER_4_0.cloverRec.CF[68]==0&false))) {{
            __CLOVER_4_0.cloverRec.S[449]++;return false;
        }

        }__CLOVER_4_0.cloverRec.S[450]++;if ((((!(object instanceof OpenIdService)) && (++__CLOVER_4_0.cloverRec.CT[69]!=0|true)) || (++__CLOVER_4_0.cloverRec.CF[69]==0&false))) {{
            __CLOVER_4_0.cloverRec.S[451]++;return false;
        }

        }__CLOVER_4_0.cloverRec.S[452]++;final OpenIdService service = (OpenIdService) object;

        __CLOVER_4_0.cloverRec.S[453]++;return getIdentity().equals(service.getIdentity())
            && getSignature().equals(service.getSignature())
            && this.getOriginalUrl().equals(service.getOriginalUrl());
    }finally{__CLOVER_4_0.cloverRec.D = true;}}

    public String getIdentity() {try {__CLOVER_4_0.cloverRec.M[117]++;
        __CLOVER_4_0.cloverRec.S[454]++;return this.identity;
    }finally{__CLOVER_4_0.cloverRec.D = true;}}

    public String getSignature() {try {__CLOVER_4_0.cloverRec.M[118]++;
        __CLOVER_4_0.cloverRec.S[455]++;return (((this.signature != null ) && (++__CLOVER_4_0.cloverRec.CT[70]!=0|true)) || (++__CLOVER_4_0.cloverRec.CF[70]==0&false))? this.signature : generateHash(
            "identity=" + this.identity + ",return_to=" + getOriginalUrl());
    }finally{__CLOVER_4_0.cloverRec.D = true;}}
}
