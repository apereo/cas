package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;

import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link MongoDbYubiKeyAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Document
@NoArgsConstructor
public class MongoDbYubiKeyAccount extends YubiKeyAccount {
    private static final long serialVersionUID = 1505204109111619367L;

    public MongoDbYubiKeyAccount(final long id, final List<String> deviceIdentifiers, final String username) {
        super(id, new ArrayList<>(deviceIdentifiers), username);
    }
}
