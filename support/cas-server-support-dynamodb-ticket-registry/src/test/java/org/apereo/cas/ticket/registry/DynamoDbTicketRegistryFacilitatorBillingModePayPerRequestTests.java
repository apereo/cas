package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.DynamoDbTicketRegistryConfiguration;
import org.apereo.cas.config.DynamoDbTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbTicketRegistryFacilitatorBillingModePayPerRequestTests}.
 *
 * @author Ned Regina
 *
 */
@EnabledIfPortOpen(port = 8000)
@SpringBootTest(classes = {
        DynamoDbTicketRegistryConfiguration.class,
        DynamoDbTicketRegistryTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        RefreshAutoConfiguration.class
},
        properties = {
                "cas.ticket.registry.dynamo-db.endpoint=http://localhost:8000",
                "cas.ticket.registry.dynamo-db.drop-tables-on-startup=true",
                "cas.ticket.registry.dynamo-db.local-instance=true",
                "cas.ticket.registry.dynamo-db.region=us-east-1",
                "cas.ticket.registry.dynamo-db.billingMode=PAY_PER_REQUEST"
        })
@Tag("DynamoDb")
public class DynamoDbTicketRegistryFacilitatorBillingModePayPerRequestTests {

        static {
                System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
                System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
        }

        @Autowired
        @Qualifier("dynamoDbTicketRegistryFacilitator")
        private DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator;

        @Test
        public void verifyCreateTableWithOnDemandBilling() {
                dynamoDbTicketRegistryFacilitator.createTicketTables(true);
                DynamoDbClient client = dynamoDbTicketRegistryFacilitator.getAmazonDynamoDBClient();
                dynamoDbTicketRegistryFacilitator.getTicketCatalog().findAll().forEach(td -> {
                        DescribeTableResponse resp = client.describeTable(DescribeTableRequest.builder()
                                .tableName(td.getProperties().getStorageName())
                                .build());
                        assertEquals(BillingMode.PAY_PER_REQUEST, resp.table().billingModeSummary().billingMode());
                });
        }
}
