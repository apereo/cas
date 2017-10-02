package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by tschmidt on 4/20/16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceContact extends Serializable {

    public static final long serialVersionUID = 1L;

    String getName();

    String getEMail();

    String getPhone();

    String getDepartment();
}
