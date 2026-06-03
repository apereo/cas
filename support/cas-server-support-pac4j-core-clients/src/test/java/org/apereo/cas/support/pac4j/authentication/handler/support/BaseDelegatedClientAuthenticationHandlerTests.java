package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseDelegatedClientAuthenticationHandlerTests}.
 * 
 * @author Jiří Prokop
 * @since 8.0.0
 */
@Tag("AuthenticationHandler")
public class BaseDelegatedClientAuthenticationHandlerTests {

    @Test
    public void verifyNestedMapAttributeIsPreserved() {
        val sessionStore = mock(SessionStore.class);
        val handler = new BaseDelegatedClientAuthenticationHandler("test",
            PrincipalFactoryUtils.newPrincipalFactory(), 1, sessionStore) {
            @Override
            protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) {
                throw new UnsupportedOperationException("Not implemented for this test");
            }
        };

        val profile = new CommonProfile();
        profile.setId("testuser");
        profile.addAttribute("username", "admin");

        val addressMap = Map.of("formatted", "GAJDOŠOVA 123, BRNO", "region", "BRNO-MĚSTO");
        profile.addAttribute("address", addressMap);

        val credentials = new ClientCredential(mock(Credentials.class), "TestClient");
        val client = mock(BaseClient.class);
        when(client.getName()).thenReturn("TestClient");

        val result = handler.createResult(credentials, profile, client, mock(Service.class));

        assertNotNull(result);
        val principal = result.getPrincipal();
        assertNotNull(principal);

        val attributes = principal.getAttributes();

        assertTrue(attributes.containsKey("username"));
        assertEquals(List.of("admin"), attributes.get("username"));

        assertTrue(attributes.containsKey("address"));
        val addressAttribute = attributes.get("address");
        assertEquals(1, addressAttribute.size());

        val addressValue = addressAttribute.get(0);
        assertTrue(addressValue instanceof Map);

        @SuppressWarnings("unchecked")
        var resultingMap = (Map<String, Object>) addressValue;
        assertEquals("GAJDOŠOVA 123, BRNO", resultingMap.get("formatted"));
        assertEquals("BRNO-MĚSTO", resultingMap.get("region"));
    }

    @Test
    public void verifyMultipleNestedMapAttributes() {
        val sessionStore = mock(SessionStore.class);
        val handler = new BaseDelegatedClientAuthenticationHandler("test",
            PrincipalFactoryUtils.newPrincipalFactory(), 1, sessionStore) {
            @Override
            protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) {
                throw new UnsupportedOperationException("Not implemented for this test");
            }
        };

        val profile = new CommonProfile();
        profile.setId("user123");

        val addressMap = Map.of("street", "Main St", "city", "Brno", "zip", "60200");
        val phoneMap = Map.of("countryCode", "+420", "number", "123456789");

        profile.addAttribute("address", addressMap);
        profile.addAttribute("phone", phoneMap);
        profile.addAttribute("name", "John Doe");

        val credentials = new ClientCredential(mock(Credentials.class), "TestClient");
        val client = mock(BaseClient.class);
        when(client.getName()).thenReturn("TestClient");

        val result = handler.createResult(credentials, profile, client, mock(Service.class));

        assertNotNull(result);
        val principal = result.getPrincipal();
        assertNotNull(principal);

        val attributes = principal.getAttributes();

        assertEquals(List.of("John Doe"), attributes.get("name"));

        assertTrue(attributes.containsKey("address"));
        var addressAttr = attributes.get("address");
        assertEquals(1, addressAttr.size());
        @SuppressWarnings("unchecked")
        var addressMapResult = (Map<String, Object>) addressAttr.get(0);
        assertEquals("Main St", addressMapResult.get("street"));
        assertEquals("Brno", addressMapResult.get("city"));
        assertEquals("60200", addressMapResult.get("zip"));

        assertTrue(attributes.containsKey("phone"));
        var phoneAttr = attributes.get("phone");
        assertEquals(1, phoneAttr.size());
        @SuppressWarnings("unchecked")
        var phoneMapResult = (Map<String, Object>) phoneAttr.get(0);
        assertEquals("+420", phoneMapResult.get("countryCode"));
        assertEquals("123456789", phoneMapResult.get("number"));
    }

    @Test
    public void verifyDeeplyNestedMapAttribute() {
        val sessionStore = mock(SessionStore.class);
        val handler = new BaseDelegatedClientAuthenticationHandler("test",
            PrincipalFactoryUtils.newPrincipalFactory(), 1, sessionStore) {
            @Override
            protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) {
                throw new UnsupportedOperationException("Not implemented for this test");
            }
        };

        val profile = new CommonProfile();
        profile.setId("user456");

        val locationMap = Map.of(
            "lat", 49.1951,
            "lng", 16.6068,
            "details", Map.of("source", "GPS", "accuracy", "high")
        );
        profile.addAttribute("location", locationMap);

        val credentials = new ClientCredential(mock(Credentials.class), "TestClient");
        val client = mock(BaseClient.class);
        when(client.getName()).thenReturn("TestClient");

        val result = handler.createResult(credentials, profile, client, mock(Service.class));

        assertNotNull(result);
        val principal = result.getPrincipal();
        assertNotNull(principal);

        val attributes = principal.getAttributes();

        assertTrue(attributes.containsKey("location"));
        var locationAttr = attributes.get("location");
        assertEquals(1, locationAttr.size());

        @SuppressWarnings("unchecked")
        var locationMapResult = (Map<String, Object>) locationAttr.get(0);
        assertEquals(49.1951, locationMapResult.get("lat"));
        assertEquals(16.6068, locationMapResult.get("lng"));

        var detailsValue = locationMapResult.get("details");
        assertTrue(detailsValue instanceof Map);
        @SuppressWarnings("unchecked")
        var detailsMap = (Map<String, Object>) detailsValue;
        assertEquals("GPS", detailsMap.get("source"));
        assertEquals("high", detailsMap.get("accuracy"));
    }
}
