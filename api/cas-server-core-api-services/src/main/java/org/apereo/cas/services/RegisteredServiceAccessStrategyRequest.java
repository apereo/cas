package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ApplicationContext;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RegisteredServiceAccessStrategyRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@With
@RequiredArgsConstructor
public class RegisteredServiceAccessStrategyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1445340717880423332L;

    private final Service service;

    private final RegisteredService registeredService;

    private final String principalId;

    @Builder.Default
    private final Map<String, List<Object>> attributes = new HashMap<>();

    @JsonIgnore
    private final ApplicationContext applicationContext;
}
