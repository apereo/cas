package org.jasig.cas.authentication.token;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>A {@linkplain Token} is derived from data received from a client. This
 * data is a {@link Base64} encoded string. When decoded, this string is an
 * AES encrypted string that decrypts to another string that represents a
 * {@link JSONObject} with the following properties:</p>
 * <p/>
 * <ul>
 * <li>
 * {@code generated}: a timestamp, in milliseconds, at which the token
 * was created
 * </li>
 * <li>
 * {@code credentials}: a serialized {@link TokenAttributes} object
 * </li>
 * </ul>
 * <p/>
 * <p>Initially, the {@linkplain Token} is not decrypted. Decryption will be
 * attempted when either the
 * {@link Token#getGenerated()} or
 * {@link Token#getAttributes()} methods are
 * invoked. Thus, it is imperative that
 * {@link Token#setKey(TokenKey)}
 * method be invoked prior to accessing either of these properties.</p>
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class Token {
    private static final Logger LOGGER = LoggerFactory.getLogger(Token.class);

    private TokenKey key;
    private final String tokenData;
    private boolean isDecoded;

    private long generated;
    private TokenAttributes attributes;
    private List requiredTokenAttributes;
    private Map tokenAttributesMap;

    /**
     * Initializes a {@linkplain Token} object from a
     * Base64-encoded data string.
     *
     * @param data The data to decode into a {@linkplain Token}.
     */
    public Token(final String data) {
        this.tokenData = data;
    }

    /**
     * Retrieve the {@link TokenAttributes} object associated with this
     * {@link Token}. The attributes include the user's username, first name,
     * last name, and email address.
     *
     * @return A valid {@link TokenAttributes} object or null if not decrypted.
     */
    public TokenAttributes getAttributes() {
        if (this.isDecoded) {
            return this.attributes;
        }

        try {
            this.decryptData();
            return this.attributes;
        } catch (final Exception e) {
            LOGGER.error("No TokenAttributes available", e);
        }
        return null;
    }

    /**
     * Return the timestamp, in milliseconds, when the {@linkplain Token} was
     * generated.
     *
     * @return The timestamp from the client or the epoch if not decrypted.
     */
    public long getGenerated() {
        if (this.isDecoded) {
            return this.generated;
        }

        try {
            this.decryptData();
            return this.generated;
        } catch (final Exception e) {
            LOGGER.error("No generated timestamp available", e);
        }
        return new Date().getTime();
    }

    /**
     * Define the crypto key that will be used to decode the {@linkplain Token}
     * data.
     *
     * @param key A valid {@link TokenKey} object.
     */
    public void setKey(final TokenKey key) {
        this.key = key;
    }

    public void setRequiredTokenAttributes(final List requiredTokenAttributes) {
        this.requiredTokenAttributes = requiredTokenAttributes;
    }

    public void setTokenAttributesMap(final Map tokenAttributesMap) {
        this.tokenAttributesMap = tokenAttributesMap;
    }

    private void decryptData() throws Exception {
        try {
            LOGGER.debug("Decrypting token [{}] with key [{}]",
                    this.key.getName(),
                    new String(this.key.getData().read()));
            final String decryptedString = Crypto.decryptEncodedStringWithKey(this.tokenData, this.key);
            final JSONObject jsonObject = new JSONObject(decryptedString);
            LOGGER.debug("Decrypted token: {}", jsonObject);

            this.generated = jsonObject.getLong("generated");
            this.attributes = new TokenAttributes(
                    jsonObject.getJSONObject("credentials").toString(),
                    this.requiredTokenAttributes,
                    this.tokenAttributesMap
            );
            this.isDecoded = true;
            LOGGER.debug("Token is successfully decrypted.");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
