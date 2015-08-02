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

package org.jasig.cas.adaptors.yubikey;

import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadUsernameOrPasswordAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;

/**
 * An authentication handler that uses the Yubico cloud validation
 * platform to authenticate one-time password tokens that are
 * issued by a YubiKey device. To use YubiCloud you need a
 * client id and an API key which must be obtained from Yubico.
 *
 * <p>For more info, please visit
 * <a href="http://yubico.github.io/yubico-java-client/">this link</a></p>
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class YubiKeyAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler
        implements InitializingBean {

    private YubiKeyAccountRegistry registry = new AcceptAnyYubiKeyAccountRegistry();

    private YubicoClient client;

    /**
     * Prepares the Yubico client with the received clientId and secretKey. By default,
     * all YubiKey accounts are allowed to authenticate.
     * <p/>
     * WARNING: THIS CONSTRUCTOR RESULTS IN AN EXAMPLE YubiKeyAuthenticationHandler
     * CONFIGURATION THAT CONSIDERS ALL Yubikeys VALID FOR ALL USERS.  YOU MUST NOT USE
     * THIS CONSTRUCTOR IN PRODUCTION.
     *
     * @param clientId
     * @param secretKey
     */
    public YubiKeyAuthenticationHandler(final Integer clientId, final String secretKey) {
        this.client = YubicoClient.getClient(clientId);
        this.client.setKey(secretKey);
    }

    /**
     * Prepares the Yubico client with the received clientId and secretKey. If you wish to
     * limit the usage of this handler only to a particular set of yubikey accounts for a special
     * group of users, you may provide an compliant implementation of {@link YubiKeyAccountRegistry}.
     * By default, all accounts are allowed.
     *
     * @param clientId
     * @param secretKey
     * @param registry
     */
    public YubiKeyAuthenticationHandler(final Integer clientId, final String secretKey, final YubiKeyAccountRegistry registry) {
        this(clientId, secretKey);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.registry instanceof AcceptAnyYubiKeyAccountRegistry) {
            log.warn("{} instantiated with example accept-any configuration handled via {}. " +
                            "THIS IS NOT OKAY IN PRODUCTION. NO. NO. NO.", this.getClass().getSimpleName(),
                    AcceptAnyYubiKeyAccountRegistry.class.getSimpleName());
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Attempts to authenticate the received credentials using the Yubico cloud validation platform.
     * In this implementation, the {@link org.jasig.cas.authentication.principal.UsernamePasswordCredentials#getUsername()}
     * is mapped to the <code>uid</code> which will be used by the plugged-in instance of the {@link YubiKeyAccountRegistry}
     * and the {@link org.jasig.cas.authentication.principal.UsernamePasswordCredentials#getPassword()} is the received
     * one-time password token issued sby the YubiKey device.
     *
     * @param usernamePasswordCredentials
     *
     * @return true if the authentication succeeds. False, otherwise.
     *
     * @throws AuthenticationException
     */
    @Override
    protected boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials usernamePasswordCredentials) throws AuthenticationException {
        try {
            final String uid = usernamePasswordCredentials.getUsername();
            final String otp = usernamePasswordCredentials.getPassword();

            if (YubicoClient.isValidOTPFormat(otp)) {

                final String publicId = YubicoClient.getPublicId(otp);

                if (this.registry.isYubiKeyRegisteredFor(uid, publicId)) {
                    final YubicoResponse response = client.verify(otp);
                    log.debug("YubiKey response status {} at {}", response.getStatus(), response.getTimestamp());
                    return (response.getStatus() == YubicoResponseStatus.OK);

                }
                else {
                    log.debug("YubiKey public id [{}] is not registered for user [{}]", publicId, uid);
                }
            }
            else {
                log.debug("Invalid OTP format [{}]", otp);
            }
            return false;
        }
        catch (final Exception e) {
            throw new BadUsernameOrPasswordAuthenticationException(e);
        }

    }

    /**
     * Example implementation of YubiKeyAccountRegistry that considers all yubikey Ids
     * registered for all users.
     * THIS IS OBVIOUSLY COMPLETELY UNACCEPTABLE FOR PRODUCTION USE AND YOU MUST USE
     * A REGISTRY THAT ACTUALLY REGISTERS IN PRODUCTION.
     */
    private static final class AcceptAnyYubiKeyAccountRegistry implements YubiKeyAccountRegistry {

        @Override
        public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
            return true;
        }
    }
}
