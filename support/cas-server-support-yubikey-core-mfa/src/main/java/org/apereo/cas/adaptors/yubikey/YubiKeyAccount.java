package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.springframework.data.annotation.Id;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link YubiKeyAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@MappedSuperclass
@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "username"})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@Accessors(chain = true)
public class YubiKeyAccount implements Serializable, Cloneable {
    /**
     * username field.
     */
    public static final String FIELD_USERNAME = "username";

    /**
     * devices field.
     */
    public static final String FIELD_DEVICES = "devices";

    /**
     * Tenant field.
     */
    public static final String FIELD_TENANT = "tenant";

    
    @Serial
    private static final long serialVersionUID = 311869140885521905L;

    @Id
    @JsonProperty
    @Transient
    @Builder.Default
    private long id = -1;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "YubiKeyRegisteredDevice")
    @Builder.Default
    private List<YubiKeyRegisteredDevice> devices = new ArrayList<>();

    @Column(nullable = false)
    @JsonProperty
    private String username;

    @Column
    @JsonProperty
    private String tenant;

    @Override
    public final YubiKeyAccount clone() {
        return FunctionUtils.doUnchecked(() -> {
            val account = (YubiKeyAccount) super.clone();
            account.setDevices(getDevices()
                .stream()
                .map(YubiKeyRegisteredDevice::clone)
                .collect(Collectors.toList()));
            return account;
        });
    }
}
