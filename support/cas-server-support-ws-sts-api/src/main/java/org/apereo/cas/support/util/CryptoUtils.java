package org.apereo.cas.support.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * This is {@link CryptoUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@UtilityClass
public class CryptoUtils {

    /**
     * Gets security properties.
     *
     * @param file the file
     * @param psw  the psw
     * @return the security properties
     */
    public static Properties getSecurityProperties(final String file, final String psw) {
        return getSecurityProperties(file, psw, null);
    }

    /**
     * Gets security properties.
     *
     * @param file  the file
     * @param psw   the psw
     * @param alias the alias
     * @return the security properties
     */
    public static Properties getSecurityProperties(final String file, final String psw, final String alias) {
        val p = new Properties();
        p.put("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        p.put("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
        p.put("org.apache.ws.security.crypto.merlin.keystore.password", psw);
        p.put("org.apache.ws.security.crypto.merlin.keystore.file", file);
        if (StringUtils.isNotBlank(alias)) {
            p.put("org.apache.ws.security.crypto.merlin.keystore.alias", alias);
        }
        return p;
    }
}
