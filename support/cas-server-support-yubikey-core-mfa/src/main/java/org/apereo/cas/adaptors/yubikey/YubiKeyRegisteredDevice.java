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
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link YubiKeyRegisteredDevice}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Embeddable
@Table(name = "YubiKeyRegisteredDevice")
@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@Accessors(chain = true)
public class YubiKeyRegisteredDevice implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 221869140885521905L;

    @Id
    @GeneratedValue
    @JsonProperty
    private long id;

    @Column(nullable = false, length = 2048)
    @JsonProperty("publicId")
    private String publicId;

    @Column(nullable = false)
    @JsonProperty("name")
    private String name;

    @Column
    @JsonProperty("registrationDate")
    @Builder.Default
    private ZonedDateTime registrationDate = ZonedDateTime.now(ZoneOffset.UTC);

    @Override
    public YubiKeyRegisteredDevice clone() {
        return FunctionUtils.doUnchecked(() -> (YubiKeyRegisteredDevice) super.clone());
    }
}
