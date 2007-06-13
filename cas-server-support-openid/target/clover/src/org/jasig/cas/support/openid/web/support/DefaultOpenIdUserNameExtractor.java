/* $$ This file has been instrumented by Clover 1.3.12#20060208202937157 $$ *//*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;


/**
 * Extracts a local Id from an openid.identity. The default provider can extract
 * the following uris: http://openid.myprovider.com/scottb provides a local id
 * of scottb.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class DefaultOpenIdUserNameExtractor implements
    OpenIdUserNameExtractor {public static class __CLOVER_1_0{public static com_cenqua_clover.g cloverRec;static{try{if(20060208202937157L!=com_cenqua_clover.CloverVersionInfo.getBuildMagic()){java.lang.System.err.println("[CLOVER] WARNING: The Clover version used in instrumentation does not match the runtime version. You need to run instrumented classes against the same version of Clover that you instrumented with.");java.lang.System.err.println("[CLOVER] WARNING: Instr=1.3.12#20060208202937157,Runtime="+com_cenqua_clover.CloverVersionInfo.getReleaseNum() + "#"+com_cenqua_clover.CloverVersionInfo.getBuildMagic());}cloverRec = com_cenqua_clover.f.getRecorder(new char[] {67,58,92,119,111,114,107,115,112,97,99,101,92,99,97,115,51,92,99,97,115,45,115,101,114,118,101,114,45,115,117,112,112,111,114,116,45,111,112,101,110,105,100,92,116,97,114,103,101,116,47,99,108,111,118,101,114,47,99,108,111,118,101,114,46,100,98},1181745647584L,500, true);}catch (Throwable t) {java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised. Are you sure you have Clover in the runtime classpath? ("+t.getClass()+":"+ t.getMessage()+")");}}}

    public String extractLocalUsernameFromUri(final String uri) {try {__CLOVER_1_0.cloverRec.M[107]++;
        __CLOVER_1_0.cloverRec.S[400]++;if ((((uri == null) && (++__CLOVER_1_0.cloverRec.CT[62]!=0|true)) || (++__CLOVER_1_0.cloverRec.CF[62]==0&false))) {{
            __CLOVER_1_0.cloverRec.S[401]++;return null;
        }

        }__CLOVER_1_0.cloverRec.S[402]++;if ((((!uri.contains("/")) && (++__CLOVER_1_0.cloverRec.CT[63]!=0|true)) || (++__CLOVER_1_0.cloverRec.CF[63]==0&false))) {{
            __CLOVER_1_0.cloverRec.S[403]++;return null;
        }

        }__CLOVER_1_0.cloverRec.S[404]++;return uri.substring(uri.lastIndexOf("/") + 1);
    }finally{__CLOVER_1_0.cloverRec.D = true;}}

}
