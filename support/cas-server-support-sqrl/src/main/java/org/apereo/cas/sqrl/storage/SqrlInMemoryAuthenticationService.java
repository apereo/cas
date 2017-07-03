package org.apereo.cas.sqrl.storage;


import org.apereo.cas.sqrl.SqrlAuthorization;
import org.jsqrl.service.SqrlAuthenticationService;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SqrlInMemoryAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlInMemoryAuthenticationService implements SqrlAuthenticationService {

    private final Map<String, SqrlAuthorization> authTable;
    private final Map<String, String> nutRelationTable;

    public SqrlInMemoryAuthenticationService() {
        this.authTable = new HashMap<>();
        this.nutRelationTable = new HashMap<>();
    }

    @Override
    public Boolean createAuthenticationRequest(final String originalNut, final String ipAddress) {
        final SqrlAuthorization authorization = new SqrlAuthorization(ipAddress, null, null, false);
        authTable.put(originalNut, authorization);
        return true;
    }

    @Override
    public Boolean linkNut(final String oldNut, final String newNut) {
        final SqrlAuthorization auth = authTable.get(oldNut);
        if (auth != null) {
            nutRelationTable.put(newNut, oldNut);
        } else {
            final String authNut = nutRelationTable.get(oldNut);
            nutRelationTable.remove(oldNut);
            nutRelationTable.put(newNut, authNut);
        }
        return true;
    }

    @Override
    public Boolean authenticateNut(final String nut, final String identityKey) {
        final String authNut = nutRelationTable.get(nut);
        final SqrlAuthorization auth = authTable.get(authNut);
        auth.setIdentityKey(identityKey);
        auth.setAuthorized(true);
        return true;
    }

    @Override
    public String getAuthenticatedSqrlIdentityKey(final String nut, final String ipAddress) {
        final SqrlAuthorization auth = authTable.get(nut);
        if (auth != null
                && auth.getAuthorized()
                && auth.getIpAddress().equals(ipAddress)) {
            return auth.getIdentityKey();
        }
        return null;
    }
}
