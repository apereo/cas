/* $$ This file has been instrumented by Clover 1.3.12#20060208202937157 $$ *//*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.flow;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredentials;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.jasig.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.jasig.cas.util.annotation.NotNull;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;

import org.springframework.webflow.execution.RequestContext;

/**
 * Attempts to utilize an existing single sign on session, but only if the
 * Principal of the existing session matches the new Principal. Note that care
 * should be taken when using credentials that are automatically provided and
 * not entered by the user.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class OpenIdSingleSignOnAction extends AbstractNonInteractiveCredentialsAction {public static class __CLOVER_0_0{public static com_cenqua_clover.g cloverRec;static{try{if(20060208202937157L!=com_cenqua_clover.CloverVersionInfo.getBuildMagic()){java.lang.System.err.println("[CLOVER] WARNING: The Clover version used in instrumentation does not match the runtime version. You need to run instrumented classes against the same version of Clover that you instrumented with.");java.lang.System.err.println("[CLOVER] WARNING: Instr=1.3.12#20060208202937157,Runtime="+com_cenqua_clover.CloverVersionInfo.getReleaseNum() + "#"+com_cenqua_clover.CloverVersionInfo.getBuildMagic());}cloverRec = com_cenqua_clover.f.getRecorder(new char[] {67,58,92,119,111,114,107,115,112,97,99,101,92,99,97,115,51,92,99,97,115,45,115,101,114,118,101,114,45,115,117,112,112,111,114,116,45,111,112,101,110,105,100,92,116,97,114,103,101,116,47,99,108,111,118,101,114,47,99,108,111,118,101,114,46,100,98},1181745647584L,500, true);}catch (Throwable t) {java.lang.System.err.println("[CLOVER] FATAL ERROR: Clover could not be initialised. Are you sure you have Clover in the runtime classpath? ("+t.getClass()+":"+ t.getMessage()+")");}}}

    @NotNull
    private OpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();
    
    public void setExtractor(final OpenIdUserNameExtractor extractor) {try {__CLOVER_0_0.cloverRec.M[105]++;
        __CLOVER_0_0.cloverRec.S[390]++;this.extractor = extractor;
    }finally{__CLOVER_0_0.cloverRec.D = true;}}

    @Override
    protected Credentials constructCredentialsFromRequest(final RequestContext context) {try {__CLOVER_0_0.cloverRec.M[106]++;
        __CLOVER_0_0.cloverRec.S[391]++;final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        __CLOVER_0_0.cloverRec.S[392]++;final String userName = this.extractor
            .extractLocalUsernameFromUri(context.getRequestParameters()
                .get("openid.identity"));
        __CLOVER_0_0.cloverRec.S[393]++;final Service service = WebUtils.getService(context);
        
        __CLOVER_0_0.cloverRec.S[394]++;context.getExternalContext().getSessionMap().put("openIdLocalId", userName);
        
        // clear the service because otherwise we can fake the username
        __CLOVER_0_0.cloverRec.S[395]++;if ((((service instanceof OpenIdService && userName == null) && (++__CLOVER_0_0.cloverRec.CT[60]!=0|true)) || (++__CLOVER_0_0.cloverRec.CF[60]==0&false))) {{
            __CLOVER_0_0.cloverRec.S[396]++;context.getFlowScope().remove("service");
        }
        
        }__CLOVER_0_0.cloverRec.S[397]++;if ((((ticketGrantingTicketId == null || userName == null) && (++__CLOVER_0_0.cloverRec.CT[61]!=0|true)) || (++__CLOVER_0_0.cloverRec.CF[61]==0&false))) {{
            __CLOVER_0_0.cloverRec.S[398]++;return null;
        }
        
        }__CLOVER_0_0.cloverRec.S[399]++;return new OpenIdCredentials(
            ticketGrantingTicketId, userName);
    }finally{__CLOVER_0_0.cloverRec.D = true;}}
}
