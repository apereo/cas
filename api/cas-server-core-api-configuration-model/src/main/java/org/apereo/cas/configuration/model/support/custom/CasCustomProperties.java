package org.apereo.cas.configuration.model.support.custom;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasCustomProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-core-web", automated = true)
@JsonFilter("CasCustomProperties")
public class CasCustomProperties implements Serializable {
    private static final long serialVersionUID = 5354004353286722083L;

    /**
     * Collection of custom settings that can be utilized for a local deployment.
     * The settings should be available to CAS views and webflows
     * for altering UI and/or introducing custom behavior to any extended customized component
     * without introducing a new property namespace. This is defined as a map, where
     * the key should be the setting name and the value should be the setting value.
     */
    private Map<String, String> properties = new HashMap<>(0);
}
