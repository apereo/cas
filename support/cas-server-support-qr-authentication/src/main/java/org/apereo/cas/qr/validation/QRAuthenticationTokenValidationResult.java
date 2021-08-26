package org.apereo.cas.qr.validation;

import org.apereo.cas.authentication.Authentication;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * This is {@link QRAuthenticationTokenValidationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@SuperBuilder
public class QRAuthenticationTokenValidationResult implements Serializable {
    private static final long serialVersionUID = -2010576443419962855L;

    private final Authentication authentication;
}
