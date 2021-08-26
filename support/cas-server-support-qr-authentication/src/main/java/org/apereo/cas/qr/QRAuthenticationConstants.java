package org.apereo.cas.qr;

/**
 * This is {@link QRAuthenticationConstants}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface QRAuthenticationConstants {
    /**
     * Topic name for QR authentication.
     */
    String QR_SIMPLE_BROKER_DESTINATION_PREFIX = "/qrtopic";

    /**
     * The QR authentication device id authorized for this transaction.
     */
    String QR_AUTHENTICATION_DEVICE_ID = "QR_AUTHENTICATION_DEVICE_ID";

    /**
     * The QR authentication channel id.
     */
    String QR_AUTHENTICATION_CHANNEL_ID = "QR_AUTHENTICATION_CHANNEL_ID";
}
