package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormData implements Serializable {
    private static final long serialVersionUID = -5201796557461644152L;
    private List<String> availableAttributes = new ArrayList<>();
    private Map<String, Map<String, ?>> customComponent = new HashMap<>();

    public List<String> getAvailableAttributes() {
        return this.availableAttributes;
    }

    public void setAvailableAttributes(final List<String> availableAttributes) {
        this.availableAttributes = availableAttributes;
    }

    /**
     * Visible for serialization only. Use {@link RegisteredServiceEditBean.FormData#getCustomComponent(String)} instead.
     *
     * @return all the custom components
     */
    public Map<String, Map<String, ?>> getCustomComponent() {
        return this.customComponent;
    }

    /**
     * Get the current properties for the specified custom component. The returned {@link Map} should only contain
     * nested Maps, Arrays, and simple objects.
     *
     * @param componentName name of the component to get the properties for (this should be unique for each
     *                      component)
     * @return current custom component properties
     */
    public Map<String, ?> getCustomComponent(final String componentName) {
        return this.customComponent.get(componentName);
    }

    /**
     * This is reserved for usage by any custom components that need to present their config to the management UI.
     * The provided {@link Map} should only contain nested Maps, Arrays, and simple objects.
     *
     * @param componentName name of the component to store the properties for (this should be unique for each
     *                      component)
     * @param properties    custom component properties
     */
    public void setCustomComponent(final String componentName, final Map<String, ?> properties) {
        this.customComponent.put(componentName, properties);
    }
}
