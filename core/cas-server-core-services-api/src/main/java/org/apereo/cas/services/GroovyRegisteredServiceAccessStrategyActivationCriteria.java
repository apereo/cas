package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategyActivationCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class GroovyRegisteredServiceAccessStrategyActivationCriteria implements RegisteredServiceAccessStrategyActivationCriteria {
    @Serial
    private static final long serialVersionUID = 5228603912161923218L;

    @Override
    public boolean shouldActivate(final RegisteredServiceAccessStrategyRequest request) {
        return false;
    }
}
