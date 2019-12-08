package org.apereo.cas.services;

import org.apereo.cas.util.DateTimeUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This is {@link DefaultRegisteredServiceExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class DefaultRegisteredServiceExpirationPolicy implements RegisteredServiceExpirationPolicy {

    private static final long serialVersionUID = 5106652807554743500L;

    private boolean deleteWhenExpired;

    private boolean notifyWhenDeleted;

    private boolean notifyWhenExpired;

    private String expirationDate;

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final String expirationDate) {
        this(deleteWhenExpired, false, false, expirationDate);
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final LocalDate expirationDate) {
        this(deleteWhenExpired, false, false, expirationDate.toString());
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final LocalDateTime expirationDate) {
        this(deleteWhenExpired, false, false, expirationDate.toString());
    }

    @Override
    public boolean isExpired() {
        if (StringUtils.isBlank(this.expirationDate)) {
            return false;
        }
        val now = LocalDateTime.now(ZoneId.systemDefault());
        val expDate = DateTimeUtils.localDateTimeOf(this.expirationDate);
        LOGGER.debug("Service expiration date is [{}] while now is [{}]", expirationDate, now);
        return now.isEqual(expDate) || now.isAfter(expDate);
    }
}
