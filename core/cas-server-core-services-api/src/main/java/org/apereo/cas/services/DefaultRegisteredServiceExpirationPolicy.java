package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This is {@link DefaultRegisteredServiceExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DefaultRegisteredServiceExpirationPolicy implements RegisteredServiceExpirationPolicy {

    private static final long serialVersionUID = 5106652807554743500L;

    private boolean deleteWhenExpired;

    private boolean notifyWhenDeleted;

    private String expirationDate;


    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final String expirationDate) {
        this(deleteWhenExpired, false, expirationDate);
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final LocalDate expirationDate) {
        this(deleteWhenExpired, false, expirationDate.toString());
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final LocalDateTime expirationDate) {
        this(deleteWhenExpired, false, expirationDate.toString());
    }
}
