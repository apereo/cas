package org.apereo.cas.ticket.device;

import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * This is {@link OAuth20DeviceTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OAuth20DeviceTokenExpirationPolicy extends HardTimeoutExpirationPolicy {
    @Serial
    private static final long serialVersionUID = -1283286621686527360L;

    @JsonCreator
    public OAuth20DeviceTokenExpirationPolicy(
        @JsonProperty("timeToLive")
        final long timeToLive) {
        super(timeToLive);
    }
}
