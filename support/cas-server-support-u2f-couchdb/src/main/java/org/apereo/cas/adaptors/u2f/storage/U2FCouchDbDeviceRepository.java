package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.couchdb.u2f.CouchDbU2FDeviceRegistration;
import org.apereo.cas.couchdb.u2f.U2FDeviceRegistrationCouchDbRepository;
import org.apereo.cas.util.DateTimeUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.exceptions.U2fBadInputException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link U2FCouchDbDeviceRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
@Getter
@Setter
public class U2FCouchDbDeviceRepository extends BaseU2FDeviceRepository implements DisposableBean {

    private final U2FDeviceRegistrationCouchDbRepository couchDb;

    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        r -> new Thread(r, "U2FCouchDbDeviceRepositoryThread"));

    private boolean asynchronous;

    public U2FCouchDbDeviceRepository(final LoadingCache<String, String> requestStorage,
                                      final U2FDeviceRegistrationCouchDbRepository couchDb,
                                      final long expirationTime, final TimeUnit expirationTimeUnit,
                                      final boolean asynchronous) {
        super(requestStorage);
        this.couchDb = couchDb;
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
        this.asynchronous = asynchronous;
    }

    @Override
    public Collection<DeviceRegistration> getRegisteredDevices(final String username) {
        return couchDb.findByUsername(username).stream()
            .map(r -> {
                try {
                    return DeviceRegistration.fromJson(getCipherExecutor().decode(r.getRecord()));
                } catch (final U2fBadInputException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        val record = new U2FDeviceRegistration();
        record.setUsername(username);
        record.setRecord(getCipherExecutor().encode(registration.toJsonWithAttestationCert()));
        record.setCreatedDate(LocalDate.now(ZoneId.systemDefault()));
        if (asynchronous) {
            this.executorService.execute(() -> couchDb.add(new CouchDbU2FDeviceRegistration(record)));
        } else {
            couchDb.add(new CouchDbU2FDeviceRegistration(record));
        }
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
        LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);
        if (asynchronous) {
            this.executorService.execute(() -> couchDb.findByDateBefore(expirationDate).forEach(couchDb::deleteRecord));
        } else {
            couchDb.findByDateBefore(expirationDate).forEach(couchDb::deleteRecord);
        }
    }

    @Override
    public void removeAll() {
        if (asynchronous) {
            this.executorService.execute(couchDb::deleteAll);
        } else {
            couchDb.deleteAll();
        }
    }

    @Override
    public void destroy() {
        this.executorService.shutdown();
    }
}
