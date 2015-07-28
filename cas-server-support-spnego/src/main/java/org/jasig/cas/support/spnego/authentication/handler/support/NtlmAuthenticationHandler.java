/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.support.spnego.authentication.handler.support;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;

/**
 * Implementation of an AuthenticationHandler for NTLM supports.
 *
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */

public class NtlmAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private static final int NBT_ADDRESS_TYPE = 0x1C;
    private static final int NTLM_TOKEN_TYPE_FIELD_INDEX = 8;
    private static final int NTLM_TOKEN_TYPE_ONE = 1;
    private static final int NTLM_TOKEN_TYPE_THREE = 3;
    private boolean loadBalance = true;

    @NotNull
    private String domainController = Config.getProperty("jcifs.smb.client.domain");

    private String includePattern;

    @Override
    protected final HandlerResult doAuthentication(
            final Credential credential) throws GeneralSecurityException, PreventedException {

        final SpnegoCredential ntlmCredential = (SpnegoCredential) credential;
        final byte[] src = ntlmCredential.getInitToken();

        UniAddress dc = null;

        boolean success = false;
        try {
            if (this.loadBalance) {
                // find the first dc that matches the includepattern
                if (this.includePattern != null) {
                    final NbtAddress[] dcs= NbtAddress.getAllByName(this.domainController, NBT_ADDRESS_TYPE, null, null);
                    for (final NbtAddress dc2 : dcs) {
                        if(dc2.getHostAddress().matches(this.includePattern)){
                            dc = new UniAddress(dc2);
                            break;
                        }
                    }
                } else {
                    dc = new UniAddress(NbtAddress.getByName(this.domainController, NBT_ADDRESS_TYPE, null));
                }
            } else {
                dc = UniAddress.getByName(this.domainController, true);
            }
            final byte[] challenge = SmbSession.getChallenge(dc);

            switch (src[NTLM_TOKEN_TYPE_FIELD_INDEX]) {
                case NTLM_TOKEN_TYPE_ONE:
                    logger.debug("Type 1 received");
                    final Type1Message type1 = new Type1Message(src);
                    final Type2Message type2 = new Type2Message(type1,
                            challenge, null);
                    logger.debug("Type 2 returned. Setting next token.");
                    ntlmCredential.setNextToken(type2.toByteArray());
                    break;
                case NTLM_TOKEN_TYPE_THREE:
                    logger.debug("Type 3 received");
                    final Type3Message type3 = new Type3Message(src);
                    final byte[] lmResponse = type3.getLMResponse() == null ? new byte[0] : type3.getLMResponse();
                    final byte[] ntResponse = type3.getNTResponse() == null ? new byte[0] : type3.getNTResponse();
                    final NtlmPasswordAuthentication ntlm = new NtlmPasswordAuthentication(
                            type3.getDomain(), type3.getUser(), challenge,
                            lmResponse, ntResponse);
                    logger.debug("Trying to authenticate {} with domain controller", type3.getUser());
                    try {
                        SmbSession.logon(dc, ntlm);
                        ntlmCredential.setPrincipal(this.principalFactory.createPrincipal(type3.getUser()));
                        success = true;
                    } catch (final SmbAuthException sae) {
                        throw new FailedLoginException(sae.getMessage());
                    }
                    break;
                default:
                    logger.debug("Unknown type: {}", src[NTLM_TOKEN_TYPE_FIELD_INDEX]);
            }
        } catch (final Exception e) {
            throw new FailedLoginException(e.getMessage());
        }

        if (!success) {
            throw new FailedLoginException();
        }
        return new DefaultHandlerResult(this, new BasicCredentialMetaData(ntlmCredential), ntlmCredential.getPrincipal());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof SpnegoCredential;
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
