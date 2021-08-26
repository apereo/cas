package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;

import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This is {@link MongoDbYubiKeyAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Document
@NoArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class MongoDbYubiKeyAccount extends YubiKeyAccount {
    private static final long serialVersionUID = 1505204109111619367L;
}
