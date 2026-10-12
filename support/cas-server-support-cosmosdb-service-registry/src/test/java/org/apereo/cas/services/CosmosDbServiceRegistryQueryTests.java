package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.cosmosdb.CosmosDbDocument;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
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
    void verifyServiceIdIsPassedAsEscapedParameter() {
        val container = mock(CosmosContainer.class);
        when(container.getId()).thenReturn("RegisteredServices");
        val items = (CosmosPagedIterable<CosmosDbDocument>) mock(CosmosPagedIterable.class);
        when(items.iterableByPage()).thenReturn(List.of());
        when(container.queryItems(any(SqlQuerySpec.class), any(CosmosQueryRequestOptions.class), eq(CosmosDbDocument.class))).thenReturn(items);
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val registry = new CosmosDbServiceRegistry(container, applicationContext, List.of());

        val serviceId = "https://app.example.org/' OR 1=1 --%_[!";
        assertNull(registry.findServiceBy(serviceId));

        val captor = ArgumentCaptor.forClass(SqlQuerySpec.class);
        verify(container).queryItems(captor.capture(), any(CosmosQueryRequestOptions.class), eq(CosmosDbDocument.class));
        val query = captor.getValue();
        assertFalse(query.getQueryText().contains("app.example.org"));
        assertEquals(1, query.getParameters().size());
        assertEquals("@serviceId", query.getParameters().getFirst().getName());
        assertEquals("%https://app.example.org/' OR 1=1 --!%!_![!!%", query.getParameters().getFirst().getValue(String.class));
    }
}
