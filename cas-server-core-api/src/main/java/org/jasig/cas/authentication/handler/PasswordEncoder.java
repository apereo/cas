package org.jasig.cas.authentication.handler;

/**
 * Interface to provide a standard way to translate a plaintext password into a
 * different representation of that password so that the password may be
 * compared with the stored encrypted password without having to decode the
 * encrypted password.
 * <p>
 * PasswordEncoders are useful because often the stored passwords are encoded
 * with a one way hash function which makes them almost impossible to decode.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface PasswordEncoder {

    /**
     * Method that actually performs the transformation of the plaintext
     * password into the encrypted password.
     *
     * @param password the password to translate
     * @return the transformed version of the password
     */
    String encode(String password);
}
