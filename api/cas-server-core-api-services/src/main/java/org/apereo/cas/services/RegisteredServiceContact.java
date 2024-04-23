package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * The interface Registered service contact.
 *
 * @author Travis Schmidt
 * @since 5.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceContact extends Serializable {

    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets email.
     *
     * @return the email
     */
    String getEmail();

    /**
     * Gets phone.
     *
     * @return the phone
     */
    String getPhone();

    /**
     * Gets department.
     *
     * @return the department
     */
    String getDepartment();

    /**
     * Gets contact type.
     *
     * @return the type
     */
    String getType();
}
