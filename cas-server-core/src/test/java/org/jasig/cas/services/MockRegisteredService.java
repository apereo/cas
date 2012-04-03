package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;

public class MockRegisteredService extends AbstractRegisteredService {
    private static final long serialVersionUID = 4036877894594884813L;

    public boolean matches(Service service) {
        return true;
    }

    @Override
    public void setServiceId(String id) {
        this.serviceId = id;
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new MockRegisteredService();
    }
}
