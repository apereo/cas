package org.apereo.cas.configuration.model.core.config.standalone;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

import java.io.Serializable;

/**
 * This is {@link StandaloneConfigurationSecurityProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-configuration", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("StandaloneConfigurationSecurityProperties")
public class StandaloneConfigurationSecurityProperties implements Serializable {
    private static final long serialVersionUID = 8571848605614437022L;

    /**
     * Algorithm to use when deciphering settings. Default algorithm is {@code PBEWithMD5AndTripleDES}.
     */
    private String alg;

    /**
     * Security provider to use when deciphering settings.
     * Leave blank for Java, {@code BC} for BouncyCastle.
     * This property can be set as a Java system property: {@code cas.standalone.configuration-security.provider}.
     */
    private String provider;

    /**
     * Total number of iterations to use when deciphering settings.
     * Default value comes from Jasypt {@value StandardPBEByteEncryptor#DEFAULT_KEY_OBTENTION_ITERATIONS}
     */
    private long iteration;

    /**
     * Secret key/password to use when deciphering settings.
     * This property can be set as a Java system property: {@code cas.standalone.configuration-security.psw}.
     */
    private String psw;

    /**
     * An initialization vector is required for {@code PBEWithDigestAndAES} algorithms that aren't BouncyCastle.
     * Enabling an initialization vector will break passwords encrypted without one.
     * Toggling this value will make pre-existing non-{@code PBEWithDigestAndAES} encrypted passwords not work.
     * For non-BouncyCastle {@code PBEWithDigestAndAES} algorithms that require an initialization vector, one will be used
     * regardless of this setting since backwards compatibility with existing passwords using those algorithms is not
     * an issue (since they didn't work in previous CAS versions).
     * The default value is false so as not to break existing encrypted passwords.
     * In general the use of an initialization vector will increase the encrypted text's length.
     */
    private Boolean initializationVector;
}
