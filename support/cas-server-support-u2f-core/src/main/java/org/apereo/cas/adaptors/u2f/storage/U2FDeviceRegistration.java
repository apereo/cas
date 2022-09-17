package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;

/**
 * This is {@link U2FDeviceRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
public class U2FDeviceRegistration implements Serializable, Cloneable {
    @Serial
    private static final long serialVersionUID = 7258490070277856614L;

    @Id
    @Transient
    @Builder.Default
    @JsonProperty("id")
    private long id = System.currentTimeMillis();

    @Column(nullable = false)
    @JsonProperty("username")
    private String username;

    @Lob
    @Column(name = "record", length = Integer.MAX_VALUE)
    @JsonProperty("record")
    private String record;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Builder.Default
    @JsonProperty("createdDate")
    private LocalDate createdDate = LocalDate.now(Clock.systemUTC());

    public U2FDeviceRegistration() {
        setId(System.nanoTime());
    }

    @Override
    @JsonIgnore
    public U2FDeviceRegistration clone() {
        return FunctionUtils.doUnchecked(() -> (U2FDeviceRegistration) super.clone());
    }

    /**
     * Matches device.
     *
     * @param device the device
     * @return true/false
     */
    public boolean matches(final U2FDeviceRegistration device) {
        return FunctionUtils.doUnchecked(() -> {
            if (device.getUsername().equals(getUsername())) {
                val requested = DeviceRegistration.fromJson(device.getRecord());
                val current = DeviceRegistration.fromJson(getRecord());
                return requested.equals(current);
            }
            return false;
        });
    }
}
