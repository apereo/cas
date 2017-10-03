package org.apereo.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by tschmidt on 4/20/16.
 */
@Entity
@Table(name="RegisteredServiceImplContact")
public class DefaultRegisteredServiceContact implements RegisteredServiceContact {

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @Column(name="name")
    private String name;

    @Column(name="email")
    private String email;

    @Column(name="phone")
    private String phone;

    @Column(name="department")
    private String department;

    public DefaultRegisteredServiceContact() {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEMail() {
        return email;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public String getDepartment() {
        return department;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public void setDepartment(final String department) {
        this.department = department;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if(!(obj instanceof DefaultRegisteredServiceContact)) {
            return false;
        }

        final DefaultRegisteredServiceContact that = (DefaultRegisteredServiceContact)obj;

        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .append(that.name, name)
                .append(that.email,email)
                .append(that.phone, phone)
                .append(that.department, department)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 31)
                .append(name)
                .append(email)
                .append(phone)
                .append(department)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("name:")
                .append(name)
                .append("email:")
                .append(email)
                .append("phone:")
                .append(phone)
                .append("department:")
                .append(department)
                .toString();
    }
}
