package org.apereo.cas.scim.v2.provisioning;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.scim.ScimProvisioningProperties;
import org.apereo.cas.configuration.model.support.scim.ScimProvisioningProperties.ScimUserSchema;
import org.apereo.cas.util.CollectionUtils;
import de.captaingoldfish.scim.sdk.common.resources.EnterpriseUser;
import de.captaingoldfish.scim.sdk.common.resources.Group;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.resources.complex.Name;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.Address;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.Email;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.Entitlement;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.Ims;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.Member;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.PersonRole;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link DefaultScimPrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class DefaultScimPrincipalAttributeMapper implements ScimPrincipalAttributeMapper {
    private final ScimProvisioningProperties scimProperties;

    @Override
    public User forCreate(final Principal principal, final Credential credential) {
        val user = new User();
        mapUser(user, principal, credential);
        return user;
    }

    @Override
    public User forUpdate(final User user, final Principal principal, final Credential credential) {
        mapUser(user, principal, credential);
        return user;
    }
    
    @Override
    public List<Group> forGroups(final Principal principal, final User... users) {
        val members = Arrays.stream(users)
            .map(user -> Member.builder()
                .ref(user.getId().orElseThrow())
                .build())
            .toList();
        val listOfGroups = new ArrayList<Group>();
        mapAttributesToSchema(principal, ScimUserSchema.GROUPS, value -> listOfGroups.add(
            Group.builder().displayName(value).members(members).build()));
        return listOfGroups;
    }
    
    private void mapUser(final User user, final Principal principal, final Credential credential) {
        user.setUserName(principal.getId());
        if (credential instanceof final UsernamePasswordCredential instance) {
            user.setPassword(instance.toPassword());
        }
        user.setActive(Boolean.TRUE);
        /*
            In SCIM specification, where the groups attribute is typically marked as read-only.
            This means that we cannot assign groups directly to a user by setting the groups attribute.
            The groups attribute is a derived attribute that reflects the groups to which the user belongs,
            and it is managed by the SCIM service provider. You should assign a user to a group by updating
            the group resource itself rather than trying to modify the groups attribute of the user.
         */
        user.setGroups(List.of());

        mapAttributeToSchema(principal, ScimUserSchema.NICKNAME, user::setNickName);
        mapAttributeToSchema(principal, ScimUserSchema.DISPLAY_NAME, user::setDisplayName);
        mapAttributeToSchema(principal, ScimUserSchema.EXTERNAL_ID, user::setExternalId);

        val name = new Name();
        mapAttributeToSchema(principal, ScimUserSchema.GIVEN_NAME, name::setGivenName);
        mapAttributeToSchema(principal, ScimUserSchema.FAMILY_NAME, name::setFamilyName);
        mapAttributeToSchema(principal, ScimUserSchema.MIDDLE_NAME, name::setMiddleName);
        if (!name.isEmpty()) {
            user.setName(name);
        }

        mapAttributesToSchema(principal, ScimUserSchema.EMAIL, value -> {
            val email = new Email();
            email.setValue(value);
            user.addEmail(email);
        });
        mapAttributesToSchema(principal, ScimUserSchema.PHONE_NUMBER, value -> {
            val phone = new PhoneNumber();
            phone.setValue(value);
            user.addPhoneNumber(phone);
        });
        mapAttributesToSchema(principal, ScimUserSchema.ENTITLEMENTS, value ->
            user.addEntitlement(new Entitlement.EntitlementBuilder().value(value).build()));
        mapAttributesToSchema(principal, ScimUserSchema.ROLES, value ->
            user.addRole(new PersonRole.PersonRoleBuilder().value(value).build()));
        mapAttributesToSchema(principal, ScimUserSchema.ADDRESSES, value ->
            user.addAddress(new Address.AddressBuilder().formatted(value).build()));
        mapAttributesToSchema(principal, ScimUserSchema.IMS, value ->
            user.addIms(Ims.builder().value(value).build()));

        val enterpriseUserBuilder = EnterpriseUser.builder();
        mapAttributeToSchema(principal, ScimUserSchema.ENTERPRISE_COST_CENTER, enterpriseUserBuilder::costCenter);
        mapAttributeToSchema(principal, ScimUserSchema.ENTERPRISE_DIVISION, enterpriseUserBuilder::division);
        mapAttributeToSchema(principal, ScimUserSchema.ENTERPRISE_DEPARTMENT, enterpriseUserBuilder::department);
        mapAttributeToSchema(principal, ScimUserSchema.ENTERPRISE_ORGANIZATION, enterpriseUserBuilder::organization);
        mapAttributeToSchema(principal, ScimUserSchema.ENTERPRISE_EMPLOYEE_NUMBER, enterpriseUserBuilder::employeeNumber);
        val enterpriseUser = enterpriseUserBuilder.build();
        if (!enterpriseUser.isEmpty()) {
            user.setEnterpriseUser(enterpriseUser);
        }
        if (user.getMeta().isEmpty()) {
            val meta = new Meta();
            meta.setCreated(LocalDateTime.now(Clock.systemUTC()));
            mapAttributeToSchema(principal, ScimUserSchema.RESOURCE_TYPE, meta::setResourceType);
            user.setMeta(meta);
        }
    }

    protected void mapAttributeToSchema(final Principal principal,
                                        final ScimUserSchema schema,
                                        final Consumer<String> consumer) {
        Optional.ofNullable(scimProperties.getSchemaMappings().get(schema.getName()))
            .map(attribute -> getPrincipalAttributeValue(principal, attribute))
            .filter(StringUtils::isNotBlank)
            .ifPresent(consumer);
    }

    protected void mapAttributesToSchema(final Principal principal,
                                         final ScimUserSchema schema,
                                         final Consumer<String> consumer) {
        Optional.ofNullable(scimProperties.getSchemaMappings().get(schema.getName()))
            .map(attribute -> getPrincipalAttributeValues(principal, attribute))
            .ifPresent(values -> values.forEach(value -> consumer.accept(value.toString())));

    }

    protected String getPrincipalAttributeValue(final Principal principal, final String attributeName) {
        val values = getPrincipalAttributeValues(principal, attributeName);
        if (values.isEmpty()) {
            return null;
        }
        return StringUtils.defaultIfBlank(values.iterator().next().toString(), null);
    }

    protected Set<Object> getPrincipalAttributeValues(final Principal principal,
                                                      final String attributeName) {
        val attributes = principal.getAttributes();
        if (attributes.containsKey(attributeName)) {
            return CollectionUtils.toCollection(attributes.get(attributeName));
        }
        return Set.of();
    }
    
}
