package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy}.
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
public class DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy implements RegisteredServiceOAuthAccessTokenExpirationPolicy {
    private static final long serialVersionUID = 5415436756392637728L;

    private String maxTimeToLive;

    private String timeToKill;
}
