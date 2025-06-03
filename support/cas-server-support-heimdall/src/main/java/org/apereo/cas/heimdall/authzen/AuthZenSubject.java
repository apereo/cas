package org.apereo.cas.heimdall.authzen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AuthZenSubject}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Data
@NoArgsConstructor
@Validated
@ToString
@EqualsAndHashCode
@SuperBuilder
@AllArgsConstructor
@With
public class AuthZenSubject implements Serializable {
    @Serial
    private static final long serialVersionUID = -6988573141680320570L;

    private String type;
    private String id;
    private Map<String, Object> properties;

    @JsonIgnore
    public Optional<String> getIpAddress() {
        return Optional.ofNullable((String) properties.get("ip_address"));
    }

    @JsonIgnore
    public Optional<String> getDeviceId() {
        return Optional.ofNullable((String) properties.get("device_id"));
    }
}
