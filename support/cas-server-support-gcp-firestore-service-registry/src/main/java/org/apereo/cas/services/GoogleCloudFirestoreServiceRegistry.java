package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link GoogleCloudFirestoreServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class GoogleCloudFirestoreServiceRegistry extends AbstractServiceRegistry {
    private final Firestore firestore;

    private final StringSerializer<RegisteredService> serializer;

    private final String collectionName;

    public GoogleCloudFirestoreServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                               final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                               final Firestore firestore, final String collectionName) {
        super(applicationContext, serviceRegistryListeners);
        this.firestore = firestore;
        this.collectionName = collectionName;
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return FunctionUtils.doUnchecked(() -> {
            registeredService.assignIdIfNecessary();
            invokeServiceRegistryListenerPreSave(registeredService);
            LOGGER.debug("Saved registered service: [{}]", registeredService);
            val writeResult = firestore.collection(collectionName)
                .document(String.valueOf(registeredService.getId()))
                .set(Map.of("json", serializer.toString(registeredService)))
                .get();
            LOGGER.debug("Added service [{}] to [{}] @ [{}]", registeredService.getName(),
                collectionName, writeResult.getUpdateTime());
            return registeredService;
        });
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return FunctionUtils.doUnchecked(() -> {
            val updateTime = firestore.collection(collectionName)
                .document(String.valueOf(registeredService.getId())).delete().get().getUpdateTime();
            LOGGER.debug("Deleted ticket [{}] from [{}] @ [{}]", registeredService.getId(), collectionName, updateTime);
            return true;
        });
    }

    @Override
    public void deleteAll() {
        FunctionUtils.doAndHandle(_ -> {
            val col = firestore.collection(collectionName);
            firestore.recursiveDelete(col).get();
        });
    }

    @Override
    public Collection<RegisteredService> load() {
        val references = firestore.collection(collectionName).listDocuments();
        return StreamSupport.stream(references.spliterator(), false)
            .map(doc -> FunctionUtils.doUnchecked(() -> doc.get().get()))
            .map(doc -> doc.getString("json"))
            .filter(Objects::nonNull)
            .map(serializer::from)
            .collect(Collectors.toSet());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return FunctionUtils.doUnchecked(() -> {
            val documentSnapshot = firestore.collection(collectionName)
                .document(String.valueOf(id)).get().get();
            val document = documentSnapshot.getString("json");
            return StringUtils.isNotBlank(document) ? serializer.from(document) : null;
        });
    }
}
