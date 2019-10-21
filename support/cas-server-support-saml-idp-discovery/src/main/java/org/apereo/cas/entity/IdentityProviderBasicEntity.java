package org.apereo.cas.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * This is {@link IdentityProviderBasicEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Data
public class IdentityProviderBasicEntity implements Serializable {
    private static final long serialVersionUID = 8880126090019920635L;

    private String value;

    private String lang;
}
