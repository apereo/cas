package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import java.util.ArrayList;
import java.util.List;

@RequiresModule(name = "cas-server-support-validation", automated = true)
public class AuthenticationAttributeReleaseProperties {
    private List<String> neverRelease = new ArrayList<>();

    private List<String> onlyRelease = new ArrayList<>();

    public List<String> getNeverRelease() {
        return neverRelease;
    }

    public void setNeverRelease(final List<String> neverRelease) {
        this.neverRelease = neverRelease;
    }

    public List<String> getOnlyRelease() {
        return onlyRelease;
    }

    public void setOnlyRelease(final List<String> onlyRelease) {
        this.onlyRelease = onlyRelease;
    }
}
