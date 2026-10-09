package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.config.CasGoogleAuthenticatorDynamoDbAutoConfiguration;
import org.apereo.cas.configuration.model.support.mfa.gauth.DynamoDbGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
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

    @Autowired
    @Qualifier("googleAuthenticatorTokenCredentialRepositoryFacilitator")
    private DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator facilitator;

    @Autowired
    @Qualifier("amazonDynamoDbGoogleAuthenticatorClient")
    private DynamoDbClient amazonDynamoDbClient;

    @Test
    void verifyScratchCodesAndTenantRoundTrip() {
        val username = UUID.randomUUID().toString();
        val wideCode = new BigInteger("123456789012345678901234567890123456789012345678901234567890");
        val account = registry.create(username);
        account.setScratchCodes(new ArrayList<>(List.of(wideCode)));
        val id = registry.save(account).getId();
        assertEquals(List.of(wideCode), registry.get(username, id).getScratchCodes());

        val withoutCodes = registry.get(username, id);
        withoutCodes.setScratchCodes(new ArrayList<>());
        registry.update(withoutCodes);
        assertTrue(registry.get(username, id).getScratchCodes().isEmpty());

        val tenantAccount = registry.create(UUID.randomUUID().toString());
        tenantAccount.assignIdIfNecessary();
        tenantAccount.setTenant("shire");
        facilitator.store(tenantAccount);
        assertEquals("shire", facilitator.find(tenantAccount.getId()).getTenant());
    }

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
        assertNull(registry.get(removedUser, baseId + 1));
    }

    @Test
    void verifyUserIdIndexIsAddedToExistingTable() throws Throwable {
        val properties = new DynamoDbGoogleAuthenticatorMultifactorProperties();
        properties.setTableName("CredentialRepositoryIndexTests");
        val idColumn = DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator.ColumnNames.ID.getColumnName();
        DynamoDbTableUtils.createTable(amazonDynamoDbClient, properties, properties.getTableName(), true,
            List.of(AttributeDefinition.builder().attributeName(idColumn).attributeType(ScalarAttributeType.N).build()),
            List.of(KeySchemaElement.builder().attributeName(idColumn).keyType(KeyType.HASH).build()));

        val tableFacilitator = new DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator(properties, amazonDynamoDbClient);
        tableFacilitator.createTable(false);
        val table = amazonDynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(properties.getTableName()).build()).table();
        assertTrue(table.globalSecondaryIndexes().stream()
            .anyMatch(index -> DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator.USERID_INDEX_NAME.equals(index.indexName())));

        val account = registry.create(UUID.randomUUID().toString()).assignIdIfNecessary();
        tableFacilitator.store(account);
        assertEquals(1, tableFacilitator.count());
        assertEquals(1, tableFacilitator.count(account.getUsername()));
        assertEquals(1, tableFacilitator.find(account.getUsername()).size());
        assertNotNull(tableFacilitator.find(account.getUsername().toUpperCase(Locale.ENGLISH), account.getId()));
        assertNull(tableFacilitator.find(UUID.randomUUID().toString(), account.getId()));
        tableFacilitator.remove(account.getUsername());
        assertEquals(0, tableFacilitator.count(account.getUsername()));
    }

    private void saveAccount(final String username, final long id) {
        val account = registry.create(username);
        account.setId(id);
        registry.save(account);
    }
}
