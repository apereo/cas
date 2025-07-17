package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.support.syncope.BaseSyncopeSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

@RequiresModule(name = "cas-server-support-syncope-authentication")
public class SyncopePasswordManagementProperties extends BaseSyncopeSearchProperties {

    private static final long serialVersionUID = -3772274510347618493L;

}
