package org.apereo.cas.oidc.services;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.services.RegisteredServiceOidcIdTokenExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link DefaultRegisteredServiceOidcIdTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceOidcIdTokenExpirationPolicy implements RegisteredServiceOidcIdTokenExpirationPolicy {
    @Serial
    private static final long serialVersionUID = 1246436756392637728L;

    @DurationCapable
    private String timeToKill;
}
