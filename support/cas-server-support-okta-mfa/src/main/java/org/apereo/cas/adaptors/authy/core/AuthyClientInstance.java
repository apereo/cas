package org.apereo.cas.adaptors.authy.core;

import org.apereo.cas.authentication.principal.Principal;

import com.authy.AuthyApiClient;
import com.authy.api.User;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link AuthyClientInstance}.
 *
 * @author Jérémie POISSON
 * 
 */
public interface AuthyClientInstance {

    /**
     * Gets authy client instance.
     *
     * @return the authy client
     */
    AuthyApiClient getAuthyClient();

    /**
     * Gets authy error message.
     *
     * @param err the err
     * @return the authy error message
     */
    static String getErrorMessage(final com.authy.api.Error err) {
        val builder = new StringBuilder(100);
        if (err != null) {
            builder.append("Authy Error");
            if (StringUtils.isNotBlank(err.getCountryCode())) {
                builder.append(": Country Code: ").append(err.getCountryCode());
            }
            if (StringUtils.isNotBlank(err.getMessage())) {
                builder.append(": Message: ").append(err.getMessage());
            }
        } else {
            builder.append("An unknown error has occurred. Check your API key and URL settings.");
        }
        return builder.toString();
    }

    /**
     * Gets or create user.
     *
     * @param principal the principal
     * @return the or create user
     * @throws Exception the exception
     */
    User getOrCreateUser(Principal principal) throws Exception;
}
