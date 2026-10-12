package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.cosmosdb.CosmosDbDocument;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CosmosDbServiceRegistryQueryTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 * @deprecated since 8.1.0
 */
@Tag("Azure")
@Deprecated(since = "8.1.0", forRemoval = true)
class CosmosDbServiceRegistryQueryTests {
    @Test
    void verifyRegexServiceIsMatchedWithoutPassingServiceToQuery() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("^https://app\\.example\\.org/.+");
        val document = new CosmosDbDocument();
        document.setId(String.valueOf(registeredService.getId()));
        document.setServiceId(registeredService.getServiceId());
        document.setBody(new RegisteredServiceJsonSerializer(applicationContext).toString(registeredService));

        val page = (FeedResponse<CosmosDbDocument>) mock(FeedResponse.class);
        when(page.getResults()).thenReturn(List.of(document));
        val items = (CosmosPagedIterable<CosmosDbDocument>) mock(CosmosPagedIterable.class);
        when(items.iterableByPage()).thenReturn(List.of(page));
        val container = mock(CosmosContainer.class);
        when(container.getId()).thenReturn("RegisteredServices");
        when(container.queryItems(anyString(), any(CosmosQueryRequestOptions.class), eq(CosmosDbDocument.class))).thenReturn(items);
        val registry = new CosmosDbServiceRegistry(container, applicationContext, List.of());

        val serviceId = "https://app.example.org/' OR 1=1 --";
        assertEquals(registeredService.getId(), registry.findServiceBy(serviceId).getId());
        assertNull(registry.findServiceBy("https://unknown.example.org"));

        val captor = ArgumentCaptor.forClass(String.class);
        verify(container, atLeastOnce()).queryItems(captor.capture(), any(CosmosQueryRequestOptions.class), eq(CosmosDbDocument.class));
        assertTrue(captor.getAllValues().stream().noneMatch(query -> query.contains("example.org")));
    }
}
