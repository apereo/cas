---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# SCIM Attribute Resolution

The following configuration describes how to fetch and retrieve attributes from SCIM servers.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-scim" %}

{% include_cached casproperties.html properties="cas.authn.attribute-repository.scim" %}

## Mapping Attributes

The following attributes are extracted from the SCIM response as CAS attributes:

| Attribute                          |
|------------------------------------|
| `scimUserAddress0Country`          |
| `scimUserAddress0Display`          |
| `scimUserAddress0Formatted`        |
| `scimUserAddress0Locality`         |
| `scimUserAddress0PostalCode`       |
| `scimUserAddress0Ref`              |
| `scimUserAddress0Region`           |
| `scimUserAddress0StreetAddress`    |
| `scimUserAddress0Type`             |
| `scimUserDisplayName`              |
| `scimUserExternalId`               |
| `scimUserFamilyName`               |
| `scimUserFormatted`                |
| `scimUserGivenName`                |
| `scimUserHonorificPrefix`          |
| `scimUserHonorificSuffix`          |
| `scimUserMiddleName`               |
| `scimUserId`                       |
| `scimUserLocale`                   |
| `scimUserNickName`                 |
| `scimUserPreferredLanguage`        |
| `scimUserProfileUrl`               |
| `scimUserTimezone`                 |
| `scimUserTitle`                    |
| `scimUserName`                     |
| `scimUserType`                     |
| `scimUserActive`                   |
| `scimUserRole0Display`             |
| `scimUserRole0Value`               |
| `scimUserRole0Ref`                 |
| `scimUserRole0Type`                |
| `scimUserPhone0Display`            |
| `scimUserPhone0Value`              |
| `scimUserPhone0Ref`                |
| `scimUserPhone0Type`               |
| `scimUserEmail0Display`            |
| `scimUserEmail0Value`              |
| `scimUserEmail0Ref`                |
| `scimUserEmail0Type`               |
| `scimUserGroup0Display`            |
| `scimUserGroup0Value`              |
| `scimUserGroup0Ref`                |
| `scimUserGroup0Type`               |
| `scimUserIm0Display`               |
| `scimUserIm0Value`                 |
| `scimUserIm0Ref`                   |
| `scimUserIm0Type`                  |
| `scimUserEntitlement0Display`      |
| `scimUserEntitlement0Value`        |
| `scimUserEntitlement0Ref`          |
| `scimUserEntitlement0Type`         |
| `scimUserEnterpriseCostCenter`     |
| `scimUserEnterpriseDepartment`     |
| `scimUserEnterpriseDivision`       |
| `scimUserEnterpriseEmployeeNumber` |
| `scimUserEnterpriseManager`        | 
| `scimUserEnterpriseOrganization`   |
              
Note that schema attributes that may contain multiple values such as `roles` or `groups` are collecting
using an incrementing index in the attribute name. For example, `scimUserRole0Display`, `scimUserRole1Display`, etc.
