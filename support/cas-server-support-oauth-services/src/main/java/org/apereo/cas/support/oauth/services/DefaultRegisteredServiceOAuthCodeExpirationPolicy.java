package org.apereo.cas.support.oauth.services;

import org.apereo.cas.configuration.support.DurationCapable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link DefaultRegisteredServiceOAuthCodeExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Accessors(chain = true)
public class DefaultRegisteredServiceOAuthCodeExpirationPolicy implements RegisteredServiceOAuthCodeExpirationPolicy {
    @Serial
    private static final long serialVersionUID = 8435436756392637728L;

    private long numberOfUses;

    @DurationCapable
    private String timeToLive;
}
