package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.config.CasGoogleAuthenticatorDynamoDbAutoConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import software.amazon.awssdk.core.SdkSystemSetting;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    CasGoogleAuthenticatorDynamoDbAutoConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.gauth.dynamo-db.endpoint=http://localhost:8000",
        "cas.authn.mfa.gauth.dynamo-db.drop-tables-on-startup=true",
        "cas.authn.mfa.gauth.dynamo-db.local-instance=true",
        "cas.authn.mfa.gauth.dynamo-db.region=us-east-1",
        "cas.authn.mfa.gauth.dynamo-db.table-name=CredentialRepositoryTests",
        "cas.authn.mfa.gauth.dynamo-db.token-table-name=CredentialRepositoryTokenTests"
    })
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Getter
@EnabledIfListeningOnPort(port = 8000)
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
class DynamoDbGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }
    
    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;

    @Test
    void verifyDeletesLeaveOtherAccounts() {
        val removedUser = "a" + UUID.randomUUID();
        val keptUser = "z" + UUID.randomUUID();
        val baseId = RandomUtils.nextLong(1, Long.MAX_VALUE / 2);
        saveAccount(removedUser, baseId);
        saveAccount(keptUser, baseId + 1);
        saveAccount(keptUser, baseId + 2);

        registry.delete(baseId);
        assertNull(registry.get(baseId));
        assertEquals(2, registry.count(keptUser));

        saveAccount(removedUser, baseId + 3);
        registry.delete(removedUser);
        assertEquals(0, registry.count(removedUser));
        assertEquals(2, registry.count(keptUser));
        assertNotNull(registry.get(keptUser, baseId + 1));
        assertNotNull(registry.get(keptUser, baseId + 2));
    }

    private void saveAccount(final String username, final long id) {
        val account = registry.create(username);
        account.setId(id);
        registry.save(account);
    }
}
