package org.apereo.cas.util;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link CoreTicketUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class CoreTicketUtils {
    /**
     * New ticket registry cipher executor cipher executor.
     *
     * @param registry     the registry
     * @param registryName the registry name
     * @return the cipher executor
     */
    public static CipherExecutor newTicketRegistryCipherExecutor(final EncryptionRandomizedSigningJwtCryptographyProperties registry,
                                                                 final String registryName) {
        return newTicketRegistryCipherExecutor(registry, false, registryName);
    }

    /**
     * New ticket registry cipher executor cipher executor.
     *
     * @param cryptoProps         the registry
     * @param forceIfBlankKeys the force if blank keys
     * @param registryName     the registry name
     * @return the cipher executor
     */
    public static CipherExecutor newTicketRegistryCipherExecutor(
        final EncryptionRandomizedSigningJwtCryptographyProperties cryptoProps,
        final boolean forceIfBlankKeys, final String registryName) {

        val enabled = FunctionUtils.doIf(
            !cryptoProps.isEnabled() && StringUtils.isNotBlank(cryptoProps.getEncryption().getKey()) && StringUtils.isNotBlank(cryptoProps.getSigning().getKey()),
            () -> {
                LOGGER.warn("Ticket registry encryption/signing for [{}] is not enabled explicitly in the configuration, yet signing/encryption keys "
                            + "are defined for ticket operations. CAS will proceed to enable the ticket registry encryption/signing functionality. "
                            + "If you intend to turn off this behavior, consider removing/disabling the signing/encryption keys defined in settings", registryName);
                LOGGER.debug("Defined signing key is [{}], and defined encryption key is [{}]", cryptoProps.getSigning().getKey(),
                    cryptoProps.getEncryption().getKey());
                return Boolean.TRUE;
            },
            cryptoProps::isEnabled
        ).get();


        if (enabled || forceIfBlankKeys) {
            LOGGER.debug("Ticket registry encryption/signing is enabled for [{}]", registryName);
            return new DefaultTicketCipherExecutor(
                cryptoProps.getEncryption().getKey(),
                cryptoProps.getSigning().getKey(),
                cryptoProps.getAlg(),
                cryptoProps.getSigning().getKeySize(),
                cryptoProps.getEncryption().getKeySize(),
                registryName)
                .setSigningEnabled(cryptoProps.isSigningEnabled());
        }
        LOGGER.info("Ticket registry encryption/signing is turned off. This MAY NOT be safe in a clustered production environment. "
                    + "Consider using other choices to handle encryption, signing and verification of "
                    + "ticket registry tickets, and verify the chosen ticket registry does support this behavior.");
        return CipherExecutor.noOp();
    }

}
