package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasPulsarTicketRegistryAutoConfiguration;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.pulsar.autoconfigure.PulsarAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link PulsarTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@ImportAutoConfiguration({
    PulsarAutoConfiguration.class,
    CasPulsarTicketRegistryAutoConfiguration.class
})
@TestPropertySource(properties = {
    "spring.pulsar.transaction.enabled=false",
    "spring.pulsar.admin.service-url=http://localhost:8080",
    "spring.pulsar.client.service-url=pulsar://localhost:6650"
})
@Tag("Pulsar")
@EnabledIfListeningOnPort(port = 6650)
@Getter
@Execution(ExecutionMode.SAME_THREAD)
class PulsarTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier(CipherExecutor.BEAN_NAME_TICKET_REGISTRY_CIPHER_EXECUTOR)
    private CipherExecutor messageQueueCipherExecutor;

    @Override
    protected CipherExecutor setupCipherExecutor() {
        return this.messageQueueCipherExecutor;
    }

}
