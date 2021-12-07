package org.apereo.cas.pm;

import org.apereo.cas.services.RegisteredService;

public interface PasswordManagementServiceProvider {

    PasswordManagementService getPasswordChangeService(RegisteredService registeredService);
}
