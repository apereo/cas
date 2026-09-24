package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.jmx.BaseCasJmxTests;
import org.apereo.cas.jmx.authentication.AuthenticationManagedResource;
import org.apereo.cas.jmx.authentication.PrincipalAttributesCacheManagedResource;
import org.apereo.cas.jmx.services.ServicesManagerManagedResource;
import org.apereo.cas.jmx.ticket.TicketRegistryManagedResource;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasJmxConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasJmxTests.SharedTestConfiguration.class)
@Tag("JMX")
@ExtendWith(CasTestExtension.class)
class CasJmxConfigurationTests {
    @Autowired
    @Qualifier("mbeanExporter")
    private AnnotationMBeanExporter mbeanExporter;
    
    @Autowired
    @Qualifier("servicesManagerManagedResource")
    private ServicesManagerManagedResource servicesManagerManagedResource;

    @Autowired
    @Qualifier("ticketRegistryManagedResource")
    private TicketRegistryManagedResource ticketRegistryManagedResource;

    @Autowired
    @Qualifier("authenticationManagedResource")
    private AuthenticationManagedResource authenticationManagedResource;

    @Autowired
    @Qualifier("principalAttributesCacheManagedResource")
    private PrincipalAttributesCacheManagedResource principalAttributesCacheManagedResource;

    @Test
    void verifyOperation() {
        assertNotNull(this.servicesManagerManagedResource);
        assertNotNull(this.ticketRegistryManagedResource);
        assertNotNull(this.authenticationManagedResource);
        assertNotNull(this.principalAttributesCacheManagedResource);
    }

    @Test
    void verifyExportedOperations() throws Exception {
        val server = Objects.requireNonNull(mbeanExporter.getServer());
        val servicesName = new ObjectName("org.apereo.cas.jmx.services:type=ServicesManagerManagedResource,name=servicesManagerManagedResource");
        val ticketsName = new ObjectName("org.apereo.cas.jmx.ticket:type=TicketRegistryManagedResource,name=ticketRegistryManagedResource");
        val authenticationName = new ObjectName("org.apereo.cas.jmx.authentication:type=AuthenticationManagedResource,name=authenticationManagedResource");
        val cacheName = new ObjectName("org.apereo.cas.jmx.authentication:type=PrincipalAttributesCacheManagedResource,name=principalAttributesCacheManagedResource");

        assertInstanceOf(Collection.class, server.invoke(servicesName, "getServices", ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY));
        assertInstanceOf(Long.class, server.getAttribute(servicesName, "ServiceCount"));
        assertEquals(StringUtils.EMPTY, server.invoke(servicesName, "getService", new Object[]{Long.MAX_VALUE}, new String[]{"long"}));
        assertInstanceOf(Collection.class, server.invoke(ticketsName, "getTickets", ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY));
        assertInstanceOf(Long.class, server.getAttribute(ticketsName, "TicketCount"));
        assertInstanceOf(Long.class, server.getAttribute(ticketsName, "SessionCount"));
        assertInstanceOf(Long.class, server.getAttribute(ticketsName, "ServiceTicketCount"));
        assertInstanceOf(String[].class, server.invoke(ticketsName, "getTicketsByPrefix",
            new Object[]{"TGT", 10}, new String[]{String.class.getName(), "int"}));
        assertInstanceOf(String[].class, server.invoke(ticketsName, "getSessionsFor",
            new Object[]{UUID.randomUUID().toString(), 10}, new String[]{String.class.getName(), "int"}));
        assertInstanceOf(String[].class, server.invoke(authenticationName, "getAuthenticationHandlers", ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY));
        assertInstanceOf(String[].class, server.invoke(authenticationName, "getMultifactorAuthenticationProviders", ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY));
        assertEquals(Boolean.TRUE, server.getAttribute(cacheName, "Available"));

        val ticketInfo = server.getMBeanInfo(ticketsName);
        val query = Arrays.stream(ticketInfo.getOperations()).filter(op -> op.getName().equals("getTicketsByPrefix")).findFirst().orElseThrow();
        assertArrayEquals(new String[]{"prefix", "limit"}, Arrays.stream(query.getSignature()).map(MBeanParameterInfo::getName).toArray(String[]::new));
        assertTrue(Arrays.stream(ticketInfo.getAttributes()).noneMatch(MBeanAttributeInfo::isWritable));
        assertTrue(Arrays.stream(ticketInfo.getOperations()).anyMatch(op -> op.getName().equals("clean")));
        assertTrue(Arrays.stream(server.getMBeanInfo(servicesName).getOperations()).anyMatch(op -> op.getName().equals("reload")));
        assertTrue(Arrays.stream(server.getMBeanInfo(cacheName).getOperations()).anyMatch(op -> op.getName().equals("invalidate")));
        val availability = Arrays.stream(server.getMBeanInfo(authenticationName).getOperations())
            .filter(op -> op.getName().equals("isMultifactorAuthenticationProviderAvailable")).findFirst().orElseThrow();
        assertArrayEquals(new String[]{"providerId", "serviceId"},
            Arrays.stream(availability.getSignature()).map(MBeanParameterInfo::getName).toArray(String[]::new));
    }

    @Test
    void verifyOptionalComponentsAndMaintenanceOperations() throws Exception {
        try (val context = new AnnotationConfigApplicationContext()) {
            val manager = mock(ServicesManager.class);
            val service = new CasRegisteredService();
            when(manager.load()).thenReturn(List.of(service));
            context.registerBean(ServicesManager.BEAN_NAME, ServicesManager.class, () -> manager);
            context.registerBean(TicketRegistry.BEAN_NAME, TicketRegistry.class, () -> mock(TicketRegistry.class));
            context.registerBean("jmxTestMBeanServerConfigurer", BeanPostProcessor.class,
                BaseCasJmxTests.SharedTestConfiguration::jmxTestMBeanServerConfigurer);
            context.register(CasJmxAutoConfiguration.class);
            context.refresh();

            val exporter = context.getBean("mbeanExporter", AnnotationMBeanExporter.class);
            val server = Objects.requireNonNull(exporter.getServer());
            val servicesName = new ObjectName("org.apereo.cas.jmx.services:type=ServicesManagerManagedResource,name=servicesManagerManagedResource");
            val ticketsName = new ObjectName("org.apereo.cas.jmx.ticket:type=TicketRegistryManagedResource,name=ticketRegistryManagedResource");
            val cacheName = new ObjectName("org.apereo.cas.jmx.authentication:type=PrincipalAttributesCacheManagedResource,name=principalAttributesCacheManagedResource");
            assertEquals(Boolean.FALSE, server.getAttribute(cacheName, "Available"));
            assertThrows(IllegalStateException.class, context.getBean(TicketRegistryManagedResource.class)::clean);
            assertNotNull(context.getBean(AuthenticationManagedResource.class));

            val cleaner = mock(TicketRegistryCleaner.class);
            when(cleaner.clean()).thenReturn(3);
            val cache = mock(PrincipalAttributesRepositoryCache.class);
            context.getBeanFactory().registerSingleton(TicketRegistryCleaner.BEAN_NAME, cleaner);
            context.getBeanFactory().registerSingleton(PrincipalAttributesRepositoryCache.DEFAULT_BEAN_NAME, cache);

            assertEquals(1, server.invoke(servicesName, "reload", ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY));
            assertEquals(3, server.invoke(ticketsName, "clean", ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY));
            assertEquals(Boolean.TRUE, server.getAttribute(cacheName, "Available"));
            server.invoke(cacheName, "invalidate", ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY);
            verify(manager).load();
            verify(cleaner).clean();
            verify(cache).invalidate();
        }
    }
}
