/* $$ This file has been instrumented by Clover 1.3.12#20060208202937157 $$ *//*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public final class OpenIdPostUrlHandlerMapping extends SimpleUrlHandlerMapping {public static class __CLOVER_2_0{public static com_cenqua_clover.g cloverRec;static{try{if(20060208202937157L!=com_cenqua_clover.CloverVersionInfo.getBuildMagic()){java.lang.System.err.println("[CLOVER] WARNING: The Clover version used in instrumentation does not match the runtime version. You need to run instrumented classes against the same version of Clover that you instrumented with.");java.lang.System.err.println("[CLOVER] WARNING: Instr=1.3.12#20060208202937157,Runtime="+com_cenqua_clover.CloverVersionInfo.getReleaseNum() + "#"+com_cenqua_clover.CloverVersionInfo.getBuildMagic());}cloverRec = com_cenqua_clover.f.getRecorder(new char[] {67,58,92,119,111,114,107,115,112,97,99,101,92,99,97,115,51,92,99,97,115,45,115,101,114,118,101,114,45,115,117,112,112,111,114,116,45,111,112,101,110,105,100,92,116,97,114,103,101,116,47,99,108,111,118,101,114,47,99,108,111,118,101,114,46,100,98},1181745647584L,500, true);}catch (Throwable t) {java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised. Are you sure you have Clover in the runtime classpath? ("+t.getClass()+":"+ t.getMessage()+")");}}}

    @Override
    protected Object lookupHandler(final String urlPath, final HttpServletRequest request) {try {__CLOVER_2_0.cloverRec.M[108]++;
        __CLOVER_2_0.cloverRec.S[405]++;if ((((logger.isDebugEnabled()) && (++__CLOVER_2_0.cloverRec.CT[64]!=0|true)) || (++__CLOVER_2_0.cloverRec.CF[64]==0&false))) {{
            __CLOVER_2_0.cloverRec.S[406]++;logger.debug("Request Method Type: " + request.getMethod());
            __CLOVER_2_0.cloverRec.S[407]++;logger.debug("Request Parameter: " + request.getParameter("openid.mode"));
        }
        }__CLOVER_2_0.cloverRec.S[408]++;if (((("POST".equals(request.getMethod()) && "check_authentication".equals(request.getParameter("openid.mode"))) && (++__CLOVER_2_0.cloverRec.CT[65]!=0|true)) || (++__CLOVER_2_0.cloverRec.CF[65]==0&false))) {{
            __CLOVER_2_0.cloverRec.S[409]++;logger.debug("Using this Handler.");
            __CLOVER_2_0.cloverRec.S[410]++;return super.lookupHandler(urlPath, request);
        }
        
        }__CLOVER_2_0.cloverRec.S[411]++;logger.debug("Delegating to next handler.");
        
        __CLOVER_2_0.cloverRec.S[412]++;return null;
    }finally{__CLOVER_2_0.cloverRec.D = true;}}
}
