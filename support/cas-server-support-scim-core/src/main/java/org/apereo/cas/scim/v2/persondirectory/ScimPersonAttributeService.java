package org.apereo.cas.scim.v2.persondirectory;

import module java.base;
import org.apereo.cas.configuration.model.support.scim.ScimPrincipalAttributesProperties;
import org.apereo.cas.scim.v2.BaseScimService;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.MultiComplexNode;
import lombok.val;

/**
 * This is {@link ScimPersonAttributeService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class ScimPersonAttributeService extends BaseScimService<ScimPrincipalAttributesProperties> {
    public ScimPersonAttributeService(final ScimPrincipalAttributesProperties scimProperties) {
        super(scimProperties);
    }

    /**
     * Gets person from SCIM server.
     *
     * @param uid the uid
     * @return the person
     */
    public Map<String, ?> getPerson(final String uid) {
        val scimService = getScimRequestBuilder(Optional.empty());
        val response = findUser(scimService, uid);
        if (response.isSuccess() && response.getResource().getTotalResults() > 0) {
            val user = response.getResource().getListedResources().getFirst();
            return collectPersonAttributes(user);
        }
        return new HashMap<>();
    }
    
    protected Map<String, ?> collectPersonAttributes(final User user) {
        val personAttributes = new HashMap<String, Object>();
        for (var i = 0; i < user.getAddresses().size(); i++) {
            val address = user.getAddresses().get(i);
            val prefix = "scimUserAddress%s".formatted(i);
            address.getCountry().ifPresent(value -> personAttributes.put("%sCountry".formatted(prefix), value));
            address.getDisplay().ifPresent(value -> personAttributes.put("%sDisplay".formatted(prefix), value));
            address.getFormatted().ifPresent(value -> personAttributes.put("%sFormatted".formatted(prefix), value));
            address.getLocality().ifPresent(value -> personAttributes.put("%sLocality".formatted(prefix), value));
            address.getPostalCode().ifPresent(value -> personAttributes.put("%sPostalCode".formatted(prefix), value));
            address.getRef().ifPresent(value -> personAttributes.put("%sRef".formatted(prefix), value));
            address.getRegion().ifPresent(value -> personAttributes.put("%sRegion".formatted(prefix), value));
            address.getStreetAddress().ifPresent(value -> personAttributes.put("%sStreetAddress".formatted(prefix), value));
            address.getType().ifPresent(value -> personAttributes.put("%sType".formatted(prefix), value));
        }
        user.getDisplayName().ifPresent(value -> personAttributes.put("scimUserDisplayName", value));
        user.getExternalId().ifPresent(value -> personAttributes.put("scimUserExternalId", value));
        user.getName().ifPresent(value -> {
            value.getFamilyName().ifPresent(familyName -> personAttributes.put("scimUserFamilyName", familyName));
            value.getFormatted().ifPresent(formatted -> personAttributes.put("scimUserFormatted", formatted));
            value.getGivenName().ifPresent(givenName -> personAttributes.put("scimUserGivenName", givenName));
            value.getHonorificPrefix().ifPresent(honorificPrefix -> personAttributes.put("scimUserHonorificPrefix", honorificPrefix));
            value.getHonorificSuffix().ifPresent(honorificSuffix -> personAttributes.put("scimUserHonorificSuffix", honorificSuffix));
            value.getMiddleName().ifPresent(middleName -> personAttributes.put("scimUserMiddleName", middleName));
        });
        user.getId().ifPresent(value -> personAttributes.put("scimUserId", value));
        user.getLocale().ifPresent(value -> personAttributes.put("scimUserLocale", value));
        user.getNickName().ifPresent(value -> personAttributes.put("scimUserNickName", value));
        user.getPreferredLanguage().ifPresent(value -> personAttributes.put("scimUserPreferredLanguage", value));
        user.getProfileUrl().ifPresent(value -> personAttributes.put("scimUserProfileUrl", value));
        user.getTimezone().ifPresent(value -> personAttributes.put("scimUserTimezone", value));
        user.getTitle().ifPresent(value -> personAttributes.put("scimUserTitle", value));
        user.getUserName().ifPresent(value -> personAttributes.put("scimUserName", value));
        user.getUserType().ifPresent(value -> personAttributes.put("scimUserType", value));
        user.isActive().ifPresent(value -> personAttributes.put("scimUserActive", value));

        for (var i = 0; i < user.getRoles().size(); i++) {
            val role = user.getRoles().get(i);
            val prefix = "scimUserRole%s".formatted(i);
            captureNodeAsAttributes(role, prefix, personAttributes);
        }

        for (var i = 0; i < user.getPhoneNumbers().size(); i++) {
            val phone = user.getPhoneNumbers().get(i);
            val prefix = "scimUserPhone%s".formatted(i);
            captureNodeAsAttributes(phone, prefix, personAttributes);
        }

        for (var i = 0; i < user.getEmails().size(); i++) {
            val email = user.getEmails().get(i);
            val prefix = "scimUserEmail%s".formatted(i);
            captureNodeAsAttributes(email, prefix, personAttributes);
        }

        for (var i = 0; i < user.getGroups().size(); i++) {
            val group = user.getGroups().get(i);
            val prefix = "scimUserGroup%s".formatted(i);
            captureNodeAsAttributes(group, prefix, personAttributes);
        }

        for (var i = 0; i < user.getIms().size(); i++) {
            val im = user.getIms().get(i);
            val prefix = "scimUserIm%s".formatted(i);
            captureNodeAsAttributes(im, prefix, personAttributes);
        }

        for (var i = 0; i < user.getEntitlements().size(); i++) {
            val entitlement = user.getEntitlements().get(i);
            val prefix = "scimUserEntitlement%s".formatted(i);
            captureNodeAsAttributes(entitlement, prefix, personAttributes);
        }

        user.getEnterpriseUser().ifPresent(enterprise -> {
            enterprise.getCostCenter().ifPresent(value -> personAttributes.put("scimUserEnterpriseCostCenter", value));
            enterprise.getDepartment().ifPresent(value -> personAttributes.put("scimUserEnterpriseDepartment", value));
            enterprise.getDivision().ifPresent(value -> personAttributes.put("scimUserEnterpriseDivision", value));
            enterprise.getEmployeeNumber().ifPresent(value -> personAttributes.put("scimUserEnterpriseEmployeeNumber", value));
            enterprise.getManager().ifPresent(value -> personAttributes.put("scimUserEnterpriseManager", value));
            enterprise.getOrganization().ifPresent(value -> personAttributes.put("scimUserEnterpriseOrganization", value));
        });
        return personAttributes;
    }

    private static void captureNodeAsAttributes(final MultiComplexNode node,
                                                final String attributePrefix,
                                                final Map<String, Object> personAttributes) {
        node.getDisplay().ifPresent(value -> personAttributes.put("%sDisplay".formatted(attributePrefix), value));
        node.getValue().ifPresent(value -> personAttributes.put("%sValue".formatted(attributePrefix), value));
        node.getRef().ifPresent(value -> personAttributes.put("%sRef".formatted(attributePrefix), value));
        node.getType().ifPresent(value -> personAttributes.put("%sType".formatted(attributePrefix), value));
    }
}
