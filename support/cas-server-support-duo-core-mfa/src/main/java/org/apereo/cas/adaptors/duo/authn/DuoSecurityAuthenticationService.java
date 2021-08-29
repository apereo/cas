package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link DuoSecurityAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface DuoSecurityAuthenticationService extends Serializable {

    /**
     * Result key response in the duo validation payload.
     */
    String RESULT_KEY_RESPONSE = "response";

    /**
     * Result key stat in the duo validation payload.
     */
    String RESULT_KEY_STAT = "stat";

    /**
     * Result key result in the duo validation payload.
     */
    String RESULT_KEY_RESULT = "result";

    /**
     * Result key enroll_portal_url in the duo validation payload.
     */
    String RESULT_KEY_ENROLL_PORTAL_URL = "enroll_portal_url";

    /**
     * Result key status_msg in the duo validation payload.
     */
    String RESULT_KEY_STATUS_MESSAGE = "status_msg";

    /**
     * Result key code in the duo validation payload.
     */
    String RESULT_KEY_CODE = "code";

    /**
     * Result key message in the duo validation payload.
     */
    String RESULT_KEY_MESSAGE = "message";
    /**
     * Result key message_detail in the duo validation payload.
     */
    String RESULT_KEY_MESSAGE_DETAIL = "message_detail";

    /**
     * Verify the authentication response from Duo.
     *
     * @param credential signed request token
     * @return authentication result
     * @throws Exception if response verification fails
     */
    DuoSecurityAuthenticationResult authenticate(Credential credential) throws Exception;

    /**
     * Ping provider.
     *
     * @return true /false.
     */
    boolean ping();

    /**
     * Gets duo properties.
     *
     * @return the properties.
     */
    DuoSecurityMultifactorAuthenticationProperties getProperties();

    /**
     * Sign request token.
     *
     * @param uid the uid
     * @return the signed token
     */
    default Optional<String> signRequestToken(final String uid) {
        return Optional.empty();
    }

    /**
     * Gets duo user account.
     *
     * @param username the actual user name
     * @return the duo user account
     */
    DuoSecurityUserAccount getUserAccount(String username);

    default Optional<Object> getDuoClient() {
        return Optional.empty();
    }

    /**
     * Gets admin api service.
     *
     * @return the admin api service
     */
    Optional<DuoSecurityAdminApiService> getAdminApiService();
}
