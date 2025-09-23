package org.apereo.cas.configuration.model.support.passwordless.account;

import org.apereo.cas.configuration.model.support.syncope.BaseSyncopeSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link PasswordlessAuthenticationSyncopeAccountsProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiresModule(name = "cas-server-support-passwordless-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationSyncopeAccountsProperties extends BaseSyncopeSearchProperties {
    @Serial
    private static final long serialVersionUID = -173772510569438316L;
}
