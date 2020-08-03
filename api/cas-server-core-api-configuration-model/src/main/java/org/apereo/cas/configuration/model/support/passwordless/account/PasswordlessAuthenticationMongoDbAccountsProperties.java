package org.apereo.cas.configuration.model.support.passwordless.account;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link PasswordlessAuthenticationMongoDbAccountsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-passwordless-mongo")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationMongoDbAccountsProperties extends SingleCollectionMongoDbProperties {
    private static final long serialVersionUID = -6304734732383722585L;
}
