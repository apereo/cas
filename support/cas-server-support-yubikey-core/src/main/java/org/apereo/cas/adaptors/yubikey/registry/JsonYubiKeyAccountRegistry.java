package org.apereo.cas.adaptors.yubikey.registry;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link JsonYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JsonYubiKeyAccountRegistry extends PermissiveYubiKeyAccountRegistry implements DisposableBean {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final Resource jsonResource;
    
    private WatcherService watcherService;
    
    public JsonYubiKeyAccountRegistry(final Resource jsonResource,
                                      final boolean watchResource,
                                      final YubiKeyAccountValidator validator) throws Exception {
        super(getDevicesFromJsonResource(jsonResource), validator);
        this.jsonResource = jsonResource;
        
        if (ResourceUtils.isFile(this.jsonResource) && watchResource) {
            this.watcherService = new FileWatcherService(jsonResource.getFile(),
                _ -> setDevices(getDevicesFromJsonResource(jsonResource)));
            this.watcherService.start(getClass().getSimpleName());
        }
    }

    @Override
    public void destroy() {
        FunctionUtils.doIfNotNull(watcherService, WatcherService::close);
    }

    /**
     * Loads the accounts held in the given resource. The result is a concurrent map because it
     * becomes this registry's live state: registrations and removals mutate it while the whole map
     * is being serialized back to the file, and an ordinary map cannot be read and written at the
     * same time without corrupting itself or failing the write part way through.
     *
     * @param jsonResource the resource holding the accounts
     * @return the accounts, keyed by username
     */
    private static Map<String, YubiKeyAccount> getDevicesFromJsonResource(final Resource jsonResource) {
        return FunctionUtils.doUnchecked(() -> {
            if (!ResourceUtils.doesResourceExist(jsonResource)) {
                val res = jsonResource.getFile().createNewFile();
                if (res) {
                    LOGGER.debug("Created JSON resource @ [{}]", jsonResource);
                }
            }
            if (ResourceUtils.doesResourceExist(jsonResource)) {
                val file = jsonResource.getFile();
                if (file.canRead() && file.length() > 0) {
                    final Map<String, YubiKeyAccount> accounts = MAPPER.readValue(file, new TypeReference<>() {
                    });
                    return new ConcurrentHashMap<>(accounts);
                }
            } else {
                LOGGER.warn("JSON resource @ [{}] does not exist", jsonResource);
            }
            return new ConcurrentHashMap<>();
        });
    }

    @Override
    public void delete(final String uid) {
        super.delete(uid);
        writeDevicesToFile();
    }

    @Override
    public void delete(final String username, final long deviceId) {
        super.delete(username, deviceId);
        writeDevicesToFile();
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
        writeDevicesToFile();
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        this.devices.putAll(getDevicesFromJsonResource(this.jsonResource));
        return super.getAccountsInternal();
    }

    @Override
    public YubiKeyAccount save(final YubiKeyAccount yubiAccount) {
        val account = super.save(yubiAccount);
        writeDevicesToFile();
        return account;
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        val result = super.update(account);
        writeDevicesToFile();
        return result;
    }

    private void writeDevicesToFile() {
        FunctionUtils.doUnchecked(_ -> {
            val file = jsonResource.getFile();
            MAPPER.writer().withDefaultPrettyPrinter().writeValue(file, this.devices);
        });
    }
}
