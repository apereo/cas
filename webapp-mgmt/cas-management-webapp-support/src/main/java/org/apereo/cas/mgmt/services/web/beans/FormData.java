package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Form data passed onto the screen.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class FormData implements Serializable {
    private static final long serialVersionUID = -5201796557461644152L;
    private List<String> availableAttributes = new ArrayList<>();

    public List<String> getAvailableAttributes() {
        return this.availableAttributes;
    }

    public void setAvailableAttributes(final List<String> availableAttributes) {
        this.availableAttributes = availableAttributes;
    }
}
