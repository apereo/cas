/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.windows.authentication.handler.support;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.support.windows.authentication.principal.SpnegoCredentials;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of an AuthenticationHandler for NTLM supports.
 * 
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */

public class NtlmAuthenticationHandler extends
    AbstractPreAndPostProcessingAuthenticationHandler implements
    InitializingBean {

    private boolean loadBalance = true;

    private String domainController;

    protected final boolean doAuthentication(final Credentials credentials)
        throws AuthenticationException {
        final SpnegoCredentials ntlmCredentials = (SpnegoCredentials) credentials;
        final byte[] src = ntlmCredentials.getInitToken();

        UniAddress dc;

        try {

            if (this.loadBalance) {
                dc = new UniAddress(NbtAddress.getByName(this.domainController,
                    0x1C, null));
            } else {
                dc = UniAddress.getByName(this.domainController, true);
            }
            final byte[] challenge = SmbSession.getChallenge(dc);

            switch (src[8]) {
                case 1:
                    log.debug("Type 1 received");
                    final Type1Message type1 = new Type1Message(src);
                    final Type2Message type2 = new Type2Message(type1,
                        challenge, null);
                    log.debug("Type 2 returned. Setting next token.");
                    ntlmCredentials.setNextToken(type2.toByteArray());
                    return false;
                case 3:
                    log.debug("Type 3 received");
                    final Type3Message type3 = new Type3Message(src);
                    final byte[] lmResponse = type3.getLMResponse() == null
                        ? new byte[0] : type3.getLMResponse();
                    byte[] ntResponse = type3.getNTResponse() == null
                        ? new byte[0] : type3.getNTResponse();
                    final NtlmPasswordAuthentication ntlm = new NtlmPasswordAuthentication(
                        type3.getDomain(), type3.getUser(), challenge,
                        lmResponse, ntResponse);
                    log.debug("Trying to authenticate " + type3.getUser()
                        + " with domain controller");
                    try {
                        SmbSession.logon(dc, ntlm);
                        ntlmCredentials.setPrincipal(new SimplePrincipal(type3
                            .getUser()));
                        return true;
                    } catch (final SmbAuthException sae) {
                        log.debug("Authentication failed", sae);
                        return false;
                    }
            }
        } catch (final Exception e) {
            log.error(e, e);
            throw new BadCredentialsAuthenticationException(e);
        }

        return false;
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && SpnegoCredentials.class.equals(credentials.getClass());
    }

    public void setLoadBalance(final boolean loadBalance) {
        this.loadBalance = loadBalance;
    }

    public void setDomainController(final String domainController) {
        this.domainController = domainController;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.domainController == null) {
            this.domainController = Config
                .getProperty("jcifs.smb.client.domain");
        }
    }
}
