package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.DynamoDbCoreAutoConfiguration;
import org.apereo.cas.config.DynamoDbTicketRegistryConfiguration;
import org.apereo.cas.config.DynamoDbTicketRegistryTicketCatalogConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link BaseDynamoDbTicketRegistryFacilitatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {
    DynamoDbCoreAutoConfiguration.class,
    DynamoDbTicketRegistryConfiguration.class,
    DynamoDbTicketRegistryTicketCatalogConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasPersonDirectoryStubConfiguration.class,
    WebMvcAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.ticket.registry.dynamo-db.endpoint=http://localhost:8000",
        "cas.ticket.registry.dynamo-db.drop-tables-on-startup=true",
        "cas.ticket.registry.dynamo-db.local-instance=true",
        "cas.ticket.registry.dynamo-db.region=us-east-1"
    })
public abstract class BaseDynamoDbTicketRegistryFacilitatorTests {

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    @Qualifier("dynamoDbTicketRegistryFacilitator")
    protected DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator;

}
