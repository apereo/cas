package org.apereo.cas.configuration.model.support.scim;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link ScimProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-scim")
@Getter
@Setter
@Accessors(chain = true)
public class ScimProvisioningProperties extends ScimProperties {
    @Serial
    private static final long serialVersionUID = -6248690307599732834L;

    /**
     * Map of attributes that define how SCIM schema attributes
     * should be populated using CAS attributes. The key is the SCIM schema attribute
     * name and the value is the CAS attribute name. If the CAS attribute is not found,
     * the SCIM attribute is typically ignored.
     */
    @RequiredProperty
    private Map<String, String> schemaMappings = new LinkedHashMap<>();

    /**
     * Indicate whether provisioning should be asynchronous.
     */
    private boolean asynchronous;
    
    /**
     * The Scim user schema.
     */
    @RequiredArgsConstructor
    @Getter
    public enum ScimUserSchema {
        /**
         * Nickname in the user schema.
         */
        NICKNAME("nickName"),
        /**
         * Display name in the user schema.
         */
        DISPLAY_NAME("displayName"),
        /**
         * Given name in the user schema.
         */
        GIVEN_NAME("givenName"),
        /**
         * Family name in the user schema.
         */
        FAMILY_NAME("familyName"),
        /**
         * Middle name in the user schema.
         */
        MIDDLE_NAME("middleName"),
        /**
         * Email in the user schema.
         */
        EMAIL("email"),
        /**
         * Phone number in the user schema.
         */
        PHONE_NUMBER("phoneNumber"),
        /**
         * External ID in the user schema.
         */
        EXTERNAL_ID("externalId"),
        /**
         * Entitlements in the user schema.
         */
        ENTITLEMENTS("entitlements"),
        /**
         * Roles in the user schema.
         */
        ROLES("roles"),
        /**
         * Addresses in the user schema.
         */
        ADDRESSES("addresses"),
        /**
         * IMs in the user schema.
         */
        IMS("ims"),
        /**
         * Enterprise user employee number.
         */
        ENTERPRISE_EMPLOYEE_NUMBER("employeeNumber"),
        /**
         * Enterprise user cost center.
         */
        ENTERPRISE_COST_CENTER("costCenter"),
        /**
         * Enterprise user division.
         */
        ENTERPRISE_DIVISION("division"),
        /**
         * Enterprise user department.
         */
        ENTERPRISE_DEPARTMENT("department"),
        /**
         * Enterprise user organization.
         */
        ENTERPRISE_ORGANIZATION("organization"),
        /**
         * User groups in the user schema.
         */
        GROUPS("groups"),
        /**
         * Resource type in the user schema.
         */
        RESOURCE_TYPE("resourceType");

        /**
         * Attribute name.
         */
        private final String name;
    }
}
