package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorLdapAutoConfiguration;
import org.apereo.cas.config.CasOneTimeTokenAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
public abstract class BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired(required = false)
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyMultipleDevicesSurviveSaveAndUpdate() throws Throwable {
        val username = getUsernameUnderTest();
        val first = registry.create(username);
        val firstSecret = first.getSecretKey();
        val second = registry.create(username);
        val secondSecret = second.getSecretKey();
        val firstId = registry.save(first).getId();
        val secondId = registry.save(second).getId();

        val secrets = registry.get(username).stream()
            .collect(Collectors.toMap(OneTimeTokenAccount::getId, OneTimeTokenAccount::getSecretKey));
        assertEquals(Map.of(firstId, firstSecret, secondId, secondSecret), secrets);

        val toUpdate = registry.get(username, firstId);
        toUpdate.setValidationCode(123456);
        registry.update(toUpdate);

        val accounts = registry.get(username);
        assertEquals(2, accounts.size());
        assertEquals(123456, registry.get(username, firstId).getValidationCode());
        assertEquals(firstSecret, registry.get(username, firstId).getSecretKey());
        assertEquals(secondSecret, registry.get(username, secondId).getSecretKey());
    }

    @Test
    void verifyDeleteByIdAmongSimilarIds() throws Throwable {
        val otherUsername = getUsernameUnderTest();
        val username = getUsernameUnderTest();
        val id = RandomUtils.nextLong(1, Long.MAX_VALUE / 100);
        val otherId = id * 10 + 1;

        val other = registry.create(otherUsername);
        other.setId(otherId);
        registry.save(other);
        val account = registry.create(username);
        account.setId(id);
        registry.save(account);

        registry.delete(id);
        assertNull(registry.get(username, id));
        assertNotNull(registry.get(otherUsername, otherId));
    }

    protected static String getOrganizationalUnitLdif(final String baseDn) {
        val ou = baseDn.substring("ou=".length(), baseDn.indexOf(','));
        return String.format("dn: %s%n"
            + "objectClass: top%n"
            + "objectClass: organizationalUnit%n"
            + "ou: %s%n", baseDn, ou);
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasGoogleAuthenticatorLdapAutoConfiguration.class,
        CasOneTimeTokenAuthenticationAutoConfiguration.class,
        CasGoogleAuthenticatorAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class

    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }
}
