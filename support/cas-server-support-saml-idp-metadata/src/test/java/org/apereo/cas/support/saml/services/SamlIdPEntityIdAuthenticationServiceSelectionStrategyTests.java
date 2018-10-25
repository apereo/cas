package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPEntityIdAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SamlIdPEntityIdAuthenticationServiceSelectionStrategyTests {
    @Test
    public void verifyAction() {
        val factory = new WebApplicationServiceFactory();
        val strategy =
            new SamlIdPEntityIdAuthenticationServiceSelectionStrategy(factory,
                "http://localhost:8080/cas");

        val service = factory.createService("http://localhost:8080/cas/idp/profile/SAML2/Callback.+?"
            + "entityId=http%3A%2F%2Flocalhost%3A8081%2Fcallback%3Fclient_name%3DSAML2Client&SAMLRequest=PD94bWwgdmVyc2lvbj0i"
            + "MS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOkF1dGhuUmVxdWVzdCB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjI"
            + "uMDpwcm90b2NvbCIgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJodHRwOi8vbG9jYWxob3N0OjgwODEvY2FsbGJhY2s%2FY2xpZW50X25"
            + "hbWU9U0FNTDJDbGllbnQiIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvY2FzL2lkcC9wcm9maWxlL1NBTUwyL1BPU1QvU1NPIi"
            + "BGb3JjZUF1dGhuPSJmYWxzZSIgSUQ9Il96dnB5dGUzeWVyMHlxY2VsbDhsaG1rMHEyc2puMzcycXFhZGozNHUiIElzUGFzc2l2ZT0iZmFsc2UiI"
            + "Elzc3VlSW5zdGFudD0iMjAxOC0wNS0yMlQxODoyMDoyMS42NDNaIiBQcm90b2NvbEJpbmRpbmc9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIu"
            + "MDpiaW5kaW5nczpIVFRQLVBPU1QiIFByb3ZpZGVyTmFtZT0icGFjNGotc2FtbCIgVmVyc2lvbj0iMi4wIj48c2FtbDI6SXNzdWVyIHhtbG5zOn"
            + "NhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIiBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpu"
            + "YW1laWQtZm9ybWF0OmVudGl0eSIgTmFtZVF1YWxpZmllcj0iaHR0cDovL2xvY2FsaG9zdDo4MDgxL2NhbGxiYWNrP2NsaWVudF9uYW1lPVNBTU"
            + "wyQ2xpZW50Ij5odHRwOi8vbG9jYWxob3N0OjgwODEvY2FsbGJhY2s%2FY2xpZW50X25hbWU9U0FNTDJDbGllbnQ8L3NhbWwyOklzc3Vlcj48L"
            + "3NhbWwycDpBdXRoblJlcXVlc3Q%2B&RelayState=http%3A%2F%2Flocalhost%3A8081%2Fcallback%3Fclient_name%3DSAML2Client");
        val result = strategy.resolveServiceFrom(service);
        assertTrue(strategy.supports(service));
        assertEquals("http://localhost:8081/callback?client_name=SAML2Client", result.getId());
    }
}
