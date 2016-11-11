package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.authn.web.DuoWebAuthenticationService;
import org.apereo.cas.adaptors.duo.web.flow.DuoMultifactorWebflowConfigurer;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;

/**
 * This is {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    private BaseDuoAuthenticationService duoAuthenticationService;

    private String id = DuoMultifactorWebflowConfigurer.MFA_DUO_EVENT_ID;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getOrder() {
        return casProperties.getAuthn().getMfa().getDuo().getRank();
    }

    public void setDuoAuthenticationService(final BaseDuoAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    protected boolean isAvailable() {
        return this.duoAuthenticationService.canPing();
    }
}
