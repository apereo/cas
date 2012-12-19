/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;

/**
 * Implementation of an AuthenticationHandler for NTLM supports.
 * 
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @version $Revision$ $Date$
 * @since 3.1
 */

public class NtlmAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private boolean loadBalance = true;

    @NotNull
    private String domainController = Config.getProperty("jcifs.smb.client.domain");
    
    private String includePattern = null;

    protected final HandlerResult doAuthentication(final Credential credential)
            throws GeneralSecurityException, IOException {

        final SpnegoCredential ntlmCredentials = (SpnegoCredential) credential;
        final byte[] src = ntlmCredentials.getInitToken();
        UniAddress dc = null;
        try {
            if (this.loadBalance) {
            	// find the first dc that matches the includepattern
            	if(this.includePattern != null){
            		final NbtAddress [] dcs  = NbtAddress.getAllByName(this.domainController,0x1C, null,null);
                    for (final NbtAddress dc1 : dcs) {
                        if (dc1.getHostAddress().matches(this.includePattern)) {
                            dc = new UniAddress(dc1);
                            break;
                        }
                    }
            	}
            	else
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
                case 3:
                    log.debug("Type 3 received");
                    final Type3Message type3 = new Type3Message(src);
                    final byte[] lmResponse = type3.getLMResponse() == null
                        ? new byte[0] : type3.getLMResponse();
                    final byte[] ntResponse = type3.getNTResponse() == null
                        ? new byte[0] : type3.getNTResponse();
                    final NtlmPasswordAuthentication ntlm = new NtlmPasswordAuthentication(
                        type3.getDomain(), type3.getUser(), challenge,
                        lmResponse, ntResponse);
                    log.debug("Trying to authenticate " + type3.getUser()
                        + " with domain controller");
                    try {
                        SmbSession.logon(dc, ntlm);
                        final Principal principal = new SimplePrincipal(type3.getUser());
                        ntlmCredentials.setPrincipal(principal);
                        return new HandlerResult(this, principal);
                    } catch (final SmbAuthException sae) {
                        log.debug("Authentication failed due to error {}", sae);
                    }
            }
        } catch (final Exception e) {
            log.debug("Authentication failed due to error {}", e);
        }
        throw new FailedLoginException();
    }

    public boolean supports(final Credential credential) {
        return credential != null
            && SpnegoCredential.class.equals(credential.getClass());
    }

    public void setLoadBalance(final boolean loadBalance) {
        this.loadBalance = loadBalance;
    }

    public void setDomainController(final String domainController) {
        this.domainController = domainController;
    }
    
    public void setIncludePattern(final String includePattern) {
        this.includePattern = includePattern;
    }

}
