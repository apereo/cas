import {Injectable} from '@angular/core';
/**
 * Created by tschmidt on 2/10/17.
 */
/* tslint:disable */
@Injectable()
export class Messages {
  // Welcome Screen Messages

// Blocked Errors Page
  readonly screen_blocked_header = 'Access Denied';
  readonly screen_blocked_message = 'You are not authorized to access this resource. Contact your CAS administrator for more info.';

// Logout Screen Messages
  readonly screen_logout_header  = 'Logout successful';
  readonly screen_logout_success = 'You have successfully logged out of the CAS Management web application.';

// SERVICES MANAGEMENT HEADER
  readonly management_services_header_apptitle = 'CAS Management';
  readonly management_services_header_navbar_navitem_addNewService = 'Add New Service';
  readonly management_services_header_navbar_navitem_manageService = 'Manage Services';
  readonly management_services_header_navbar_navitem_sessions = 'Sessions';
  readonly management_services_header_navbar_navitem_logout = 'Log Out';

// MANAGE SERVICES TABLE
  readonly management_services_table_label_search = 'Search by Service ID, Name or Description';
  readonly management_services_table_header_serviceName = 'Service Name';
  readonly management_services_table_header_serviceId = 'Service URL';
  readonly management_services_table_header_serviceDesc = 'Service Description';
  readonly management_services_table_details_description = 'Full Description';
  readonly management_services_table_details_proxyPolicy = 'Proxy Policy';
  readonly management_services_table_details_attrPolicy = 'Attribute Policy Option';
  readonly management_services_table_details_releaseCred = 'Release Credential';
  readonly management_services_table_details_releaseProxy = 'Release Proxy ID';
  readonly management_services_table_button_edit = 'Edit';
  readonly management_services_table_button_duplicate = 'Duplicate';
  readonly management_services_table_button_delete = 'Delete';
  readonly management_services_table_modal_delete_header = 'Confirm Delete';
  readonly management_services_table_modal_delete_msgPt1 = 'You are about to permanently delete';
  readonly management_services_table_modal_delete_msgPt2 = 'Are you sure you wish to continue?';
  readonly management_services_table_modal_commit_header = 'Commit to Repository';
  readonly management_services_table_modal_commit_listheader = 'Services to be committed:';
  readonly management_services_table_modal_commit_required = 'A commit message is required';
  readonly management_services_table_modal_commit_placeholder = 'Enter a commit message';
  readonly management_services_table_modal_button_commit = 'Commit';
  readonly management_services_table_modal_button_cancel = 'Cancel';
  readonly management_services_table_modal_button_delete = 'Delete';
  readonly management_services_table_modal_button_submit = 'Submit';
  readonly management_services_table_modal_submit_header = 'Submit Request';
  readonly management_services_table_modal_submit_listheader = 'Select commits to submitted:';


  readonly registeredService_serviceId_exists = 'A Service with that Service URL already exists.';

  readonly application_title = 'Apereo Central Authentication Service';
  readonly application_errors_global = 'Please correct the errors below:';

  readonly management_services_status_deleted = 'has been successfully deleted.';
  readonly management_services_status_notdeleted = 'A problem has occurred while trying to delete the service. Be aware that the default service can not be deleted.';
  readonly management_services_status_listfail = 'Unable to retrieve service listing.';
  readonly management_services_status_evaluationOrder_notupdated = 'The service evaluation order can not be updated.';
  readonly management_services_status_committed = 'Service changes successfully committed.';
  readonly management_services_status_published = 'Service changes successfully published.';
  readonly management_services_status_notcommitted = 'A problem has occurred while trying to commit service changes. Please check system logs for additional information.';
  readonly management_services_status_notpublished = 'A problem has occurred while trying to publish services to CAS nodes.  Please check system logs for additional information.';


// EDIT/ADD SERVICE FORM
 readonly services_form_instructions = 'Please make sure to commit your changes by clicking on the Save Changes button at the bottom of the page.';
 readonly services_form_required = 'Required';

// GENERAL LABELS
 readonly services_form_label_true = 'True';
 readonly services_form_label_false = 'False';

// SPECIFIC LABELS
 readonly services_form_label_name = 'Service Name';
 readonly services_form_label_description = 'Description';
 readonly services_form_label_type = 'Service Type';
 readonly services_form_label_casClients = 'CAS Client';
 readonly services_form_label_renewalDate = 'Renewal Date';
 readonly services_form_label_theme = 'Theme';
 readonly services_form_label_serviceId = 'Service URL';
 readonly services_form_label_entityId = 'Entity ID';
 readonly services_form_label_redirect_url = 'Redirect URL';
 readonly services_form_label_consumer_url = 'Consumer URL';
 readonly services_form_label_requiredHandlers = 'Required Handlers';
 readonly services_form_label_requiresDuo = 'Requires Duo';
 readonly services_form_label_evalOrder = 'Evaluation Order';
 readonly services_form_label_logoutUrl = 'Logout URL';
 readonly services_form_label_informationUrl = 'Information URL';
 readonly services_form_label_privacyUrl = 'Privacy URL';
 readonly services_form_label_logoUrl = 'Logo URL';
 readonly services_form_label_logoutType = 'Logout Type';
 readonly services_form_label_assignedId = 'Assigned ID';
 readonly services_form_label_uap_default = 'Default';
 readonly services_form_label_uap_anon = 'Anonymous';
 readonly services_form_label_uap_anonAttribute = 'Username Attribute';
 readonly services_form_label_uap_principleAtt = 'Principle Attribute';
 readonly services_form_label_uap_saltSetting = 'Salt Setting';
 readonly services_form_label_uap_usernameAttribute = 'Username Attribute';
 readonly services_form_label_pubKey_location = 'Location';
 readonly services_form_label_pubKey_algorithm = 'Algorithm';
 readonly services_form_label_proxyPolicy_refuse = 'Refuse';
 readonly services_form_label_proxyPolicy_regex = 'Regex';
 readonly services_form_label_oauthClientSecret = 'OAuth Client Secret';
 readonly services_form_label_oauthClientId = 'OAuth Client ID';
 readonly services_form_label_oauthShowSecret = 'Show Client Secret';
 readonly services_form_label_oauthBypass = 'OAuth Bypass Approval Prompt';
 readonly services_form_label_oauthRefreshToken = 'OAuth Refresh Token Generation';
 readonly services_form_label_oauthJsonFormat = 'JSON format for access tokens';

 readonly services_form_label_oidcClientSecret = 'OIDC Client Secret';
 readonly services_form_label_oidcClientId = 'OIDC Client ID';
 readonly services_form_label_oidcShowSecret = 'Show Client Secret';
 readonly services_form_label_oidcBypass = 'OIDC Bypass Approval Prompt';
 readonly services_form_label_oidcRefreshToken = 'OIDC Refresh Token Generation';
 readonly services_form_label_oidcJsonFormat = 'JSON format for access tokens';

 readonly services_form_label_attrRelease_excludeDefault = 'Exclude default bundle of attributes for release';
 readonly services_form_label_attrRelease_releaseCredPassword = 'Authorized to release to credential password';
 readonly services_form_label_attrRelease_releaseProxyTicket = 'Authorized to release proxy granting ticket ID';
 readonly services_form_label_attrRelease_attrFilter = 'Attribute Filter Pattern';
 readonly services_form_label_attrRelease_attrFilter_excludeUnmapped = 'Exclude Unmapped Attributes';
 readonly services_form_label_attrRelease_attrFilter_completeMatch = 'Complete Match';
 readonly services_form_label_attrRelease_attrFilter_scripted = 'Script';
 readonly services_form_label_attrRelease_principleAttRepo_default = 'Default';
 readonly services_form_label_attrRelease_principleAttRepo_cached = 'Cached';
 readonly services_form_label_attrRelease_principleAttRepo_cached_timeUnit = 'Time Unit';
 readonly services_form_label_attrRelease_principleAttRepo_cached_expiration = 'Expiration';
 readonly services_form_label_attrRelease_principleAttRepo_cached_mergeStrategy = 'Merging Strategy';
 readonly services_form_label_attrRelease_policies_scriptFile = 'Script File Location';
 readonly services_form_label_attrRelease_policies_script = 'Script Engine';
 readonly services_form_label_attrRelease_policies_groovy = 'Groovy Script';
 readonly services_form_label_attrRelease_policies_denyAll = 'Deny All';
 readonly services_form_label_attrRelease_policies_returnAll = 'Return All';
 readonly services_form_label_attrRelease_policies_returnAllowed = 'Return Allowed';
 readonly services_form_label_attrRelease_policies_returnMapped = 'Return Mapped';
 readonly services_form_label_attrRelease_policies_sourceAttribute = 'Source Attribute';
 readonly services_form_label_attrRelease_policies_casAttribute = 'CAS Attribute';
 readonly services_form_label_attrRelease_policies_wsFedClaim = 'WS Federation Claim';
 readonly services_form_label_attrRelease_policies_restful = 'REST Endpoint';
 readonly services_form_label_attrRelease_policies_groovySaml = 'Script File Location';
 readonly services_form_label_attrRelease_consent_enabled = 'User Consent Enabled';
 readonly services_form_label_attrRelease_consent_excludedAttributes = 'Excluded Attributes';
 readonly services_form_label_attrRelease_consent_includeOnlyAttributes = 'Include Only Attributes';
 readonly services_form_label_attrRelease_releaseAuthenticationAttributes = 'Authorized to release authentication attributes';
 readonly services_form_label_attrRelease_entityAttributeValues = 'Metadata Entity Attribute Values';
 readonly services_form_label_attrRelease_entityAttribute = 'Metadata Entity Attribute';
 readonly services_form_label_attrRelease_entityAttributeFormat = 'Metadata Entity Attribute Format';
 readonly services_form_label_sas_authorizedCodes = 'Acceptable HTTP Codes';
 readonly services_form_label_sas_casEnabled = 'Enable Service';
 readonly services_form_label_sas_ssoEnabled = 'Allow Single Sign-On';
 readonly services_form_label_sas_requireAll = 'Require All Attributes';
 readonly services_form_label_sas_requiredAttr = 'Required Attributes';
 readonly services_form_label_sas_starttime = 'Starting Date/Time';
 readonly services_form_label_sas_endtime = 'Ending Date/Time';
 readonly services_form_label_sas_unauthorizedredirecturl = 'Unauthorized Redirect Url';
 readonly services_form_label_sas_remoteUrl = 'Remote Url';
 readonly services_form_label_sas_caseinsensitive = 'Case Insensitive';
 readonly services_form_label_sas_rejectedattributes = 'Rejected Attributes';
 readonly services_form_label_sas_rejectedattributes_name = '\'Name\'';
 readonly services_form_label_sas_rejectedattributes_value = '\'Values\'';
 readonly services_form_label_sas_selecttype = 'Select Type';
 readonly services_form_label_sas_grouper_groupfield = 'Group Field';
 readonly services_form_label_sas_surrogate_enabled = 'Surrogate Enabled';
 readonly services_form_label_sas_surrogate_ssoEnabled = 'Surrogate SSO Enabled';
 readonly services_form_label_sas_surrogate_requiredAttributes = 'Surrogate Required Attributes';
 readonly services_form_label_multiauth_providers = 'Providers';
 readonly services_form_label_multiauth_failuremode = 'Failure Mode';
 readonly services_form_label_multiauth_principalattribute_nametrigger = 'Principal Attribute Name Trigger';
 readonly services_form_label_multiauth_principalattribute_valuetomatch = 'Principal Attribute Value To Match';
 readonly services_form_label_multiauth_bypassenabled = 'Enable Bypass';

 readonly services_form_label_saml_header = 'SAML Client';
 readonly services_form_label_saml_metadata_location = 'Metadata Location';
 readonly services_form_label_saml_metadata_maximumvalidity = 'Metadata Maximum Validity';
 readonly services_form_label_saml_authenticationcontextclass = 'Authentication Context Class';
 readonly services_form_label_saml_metadata_signaturepublickeylocation = 'Metadata Public Key Location';
 readonly services_form_label_saml_signassertions = 'Sign Assertions';
 readonly services_form_label_saml_signresponses = 'Sign Responses';
 readonly services_form_label_saml_encryptassertions = 'Encrypt Assertions';
 readonly services_form_label_saml_removeEmptyEntities = 'Remove Empty Entities Descriptors';
 readonly services_form_label_saml_removeRoleless = 'Remove Empty Roles';
 readonly services_form_label_saml_metadata_pattern = 'Metadata Filter Regex';
 readonly services_form_label_saml_metadata_dir = 'Metadata Filter Criteria';
 readonly services_form_label_saml_metadata_role = 'Whitelisted Metadata Roles';
 readonly services_form_label_saml_requiredNameIdFormat = 'Required NameID Format';
 readonly services_form_label_saml_serviceProviderNameQualifier = 'Service Provider Name Qualifier';
 readonly services_form_label_saml_nameQualifier = 'Name Qualifier';
 readonly services_form_label_saml_skipAssertionNameId = 'Skip generating assertion name id';
 readonly services_form_label_saml_skipInResponseTo = 'Skip generating subject confirmation InRespsonseTo';
 readonly services_form_label_saml_skipNotOnOrAfter = 'Skip generating subject confirmation NotOnOrAfter';
 readonly services_form_label_saml_skipRecipient = 'Skip generating subject confirmation Recipient';
 readonly services_form_label_saml_skipNotBefore = 'Skip generating subject confirmation NotBefore';
 readonly services_form_label_saml_signingCredentialType = 'Signing Credential Type';

 readonly services_form_label_oidc_scopes = 'Scopes';
 readonly services_form_label_oidc_dynamic = 'Dynamically Registered';
 readonly services_form_label_oidc_dynamicDate = 'Dynam ically Registered At';
 readonly services_form_label_oidc_signToken = 'Sign ID Token';
 readonly services_form_label_oidc_implicit = 'Enable Implicit';
 readonly services_form_label_oidc_encrypt = 'Encrypt ID Token';
 readonly services_form_label_oidc_encryptEnc = 'Encryption Encoding Algorithm';
 readonly services_form_label_oidc_encryptAlg = 'Encryption Algorithm';
 readonly services_form_label_oidc_jwks = 'JSON Web Keystore';
 readonly services_form_label_oidc_user_defined_scopes = 'User Defined Scopes';
 readonly services_form_label_oidc_subject_type = 'Subject Type';
 readonly services_form_label_oidc_sector_identifier_uri = 'Sector Identifier URI';

 readonly services_form_label_expirationPolicy_expirationDate = 'Expiration Date';
 readonly services_form_label_expirationPolicy_deleteWhenExpired = 'Delete service when expired';
 readonly services_form_label_expirationPolicy_notifyWhenDeleted = 'Notifiy contacts when service is deleted';

 readonly services_form_header_page_addService = 'Add Service';
 readonly services_form_header_page_editService = 'Edit Service';
 readonly services_form_header_oauthOptions = 'OAuth Client Options Only';
 readonly services_form_header_usernameAttProvider = 'Username Attribute Provider Options';
 readonly services_form_header_pubKey = 'Public Key Options';
 readonly services_form_header_proxyPolicy = 'Proxy Policy Options';
 readonly services_form_header_attrRelPolicy = ' Attribute Release Policy Options';
 readonly services_form_header_principleAttRepo = 'Principle Attribute Repository Options';
 readonly services_form_header_attrPolicy = 'Attribute Policy Options';
 readonly services_form_header_sas = 'Service Access Strategy';
 readonly services_form_header_properties = 'Properties';
 readonly services_form_header_properties_name = '\'Name\'';
 readonly services_form_header_properties_value = '\'Values\'';
 readonly services_form_header_multiauth = 'Multifactor Authentication Policy';
 readonly services_form_header_samlclient = 'SAML Client';

 readonly services_form_button_save = 'Save Changes';
 readonly services_form_button_cancel = 'Cancel';

 readonly services_form_warning_casDisabled = 'By disabling the service it will no longer be accessible.';

 readonly services_form_alert_loadingFailed = 'An error has occurred while attempting to load your service. Please try again later.';
 readonly services_form_alert_formHasErrors = 'Form validation has failed. Please fix the errors before attempting to save again.';
 readonly services_form_alert_unableToSave = 'An error has occurred while attempting to save the service. Please try again later.';
 readonly services_form_alert_serviceAdded = 'Service has been added successfully.';
 readonly services_form_alert_serviceUpdated = 'Service has been successfully updated.';


// SERVICE FORM TOOLTIPS
 readonly services_form_tooltip_assignedId = 'Numeric identifier for this service that is auto-generated by CAS.';
 readonly services_form_tooltip_serviceId = 'A url that represents the application. This can be a regex/ant formatted url.';
 readonly services_form_tooltip_entityId = 'An string that represents the EntityId of the SAML2 SP. This can be a regex pattern.';
 readonly services_form_tooltip_redirect_url = 'A url that represents the OAuth/OIDC server to redirect to.';
 readonly services_form_tooltip_consumer_url = 'A url that represents a WS Federation Consumer URL';
 readonly services_form_tooltip_name = 'The service name.';
 readonly services_form_tooltip_description = 'The service description.';
 readonly services_form_tooltip_type = 'Everything that applies to a CAS client applies to an OAuth client too, but OAuth clients have a few more extra settings.';
 readonly services_form_tooltip_casClients = 'List any known CAS Clients used by this service.(mod_auth_cas...)';
 readonly services_form_tooltip_renewalDate = 'This is the date by which the service will need to verified active by the service contacts.';
 readonly services_form_tooltip_oauthClientSecret = 'Secret key for this OAuth client. Only applies to OAuth service types.';
 readonly services_form_tooltip_oauthClientId = 'OAuth client id for this OAuth client. Only applies to OAuth service types.';
 readonly services_form_tooltip_oauthShowSecret = 'Enable to show your OAuth Client Secret.';
 readonly services_form_tooltip_oauthBypass = 'Indicates whether the OAuth confirmation screen should be displayed before accessing the service application.';
 readonly services_form_tooltip_oauthRefreshToken = 'Indicates whether a refresh token should be generated with the access token for this OAuth client';
 readonly services_form_tooltip_oauthJsonFormat = 'Indicates whether the access token response should be in JSON format or in plain text';

  readonly services_form_tooltip_oidcClientSecret = 'Secret key for this OIDC client. Only applies to OIDC service types.';
  readonly services_form_tooltip_oidcClientId = 'OAuth client id for this OIDC client. Only applies to OIDC service types.';
  readonly services_form_tooltip_oidcShowSecret = 'Enable to show your OIDC Client Secret.';
  readonly services_form_tooltip_oidcBypass = 'Indicates whether the OIDC confirmation screen should be displayed before accessing the service application.';
  readonly services_form_tooltip_oidcRefreshToken = 'Indicates whether a refresh token should be generated with the access token for this OIDC client';
  readonly services_form_tooltip_oidcJsonFormat = 'Indicates whether the access token response should be in JSON format or in plain text';

 readonly services_form_tooltip_theme = 'A token that represents the theme that should be applied to CAS when this service asks for authentication.  Valid values are a theme name configured in the CAS deployment, a REST endpoint or a groovy file location.';
 readonly services_form_tooltip_evalOrder = 'Determines how CAS should load, sort and evaluate services per this numeric order.';
 readonly services_form_tooltip_requiredHandlers = 'Collection of authentication handler ids defined in the CAS server configuration ' +
    'that indicate the set of authentication handlers that must successfully execute before access to this service can be granted.';
 readonly services_form_tooltip_requiresDuo = 'Setting to true will require Duo authentication for this service.';
 readonly services_form_tooltip_logoutUrl = 'Url where logout requests will be sent to, for this service.';
 readonly services_form_tooltip_informationUrl = 'Url that describes help information and guides for this service.';
 readonly services_form_tooltip_privacyUrl = 'Url that describes the privacy policies for this service.';
 readonly services_form_tooltip_logoUrl = 'The logo representing this service that would be displayed on the login page. Could be a file/classpath path or an actual http url.';
 readonly services_form_tooltip_logoutType = 'Defines how CAS should execute logout operations when dealing with this service.';
 readonly services_form_tooltip_uap_anonAttribute = 'Username attribute used for anonymous id generation.';
 readonly services_form_tooltip_uap_saltSetting = 'The salt used for anonymous id generation.';
 readonly services_form_tooltip_uap_usernameAttribute = 'Username attribute used to return back to the service as the identifier.';
 readonly services_form_tooltip_pubKey_location = 'The location to the public key file used to authenticate and sign the CAS response. ' +
    'This is specifically used to encrypt and sign the credential and the proxy granting ticket returned directly in the CAS validation ' +
    'response, provided the service is authorized to release those attributes.'
 readonly services_form_tooltip_pubKey_algorithm = 'Represents a public key for a CAS registered service, used to encrypt credentials, proxy granting ticket ids for release.';
 readonly services_form_tooltip_proxyPolicy_regex = 'Matches the proxy callback url against a regex pattern. A successful match will authorize proxy authentication. ';
 readonly services_form_tooltip_attrRelease_excludeDefault = 'Indicates whether global and default attributes set to release to all applications ' +
    'should be excluded for release for this specific application.'
 readonly services_form_tooltip_attrRelease_attrFilter = 'A regex pattern used to filter attributes based on their values only. ' +
    'Values that successfully pass the filter will be available for release to the application.'
 readonly services_form_tooltip_attrRelease_attrFilter_excludeUnmapped = 'Indicates whether unmapped attributes should be removed from the final bundle.';
 readonly services_form_tooltip_attrRelease_attrFilter_completeMatch = 'Indicates whether pattern-matching should execute over the entire value region';
 readonly services_form_tooltip_attrRelease_attrFilter_scripted = 'This field can accept either an inline groovy script that will be executed or a location of an external script file that will be loaded and executed.'
 readonly services_form_tooltip_attrRelease_releaseCredPassword = 'Release credential password to the application?';
 readonly services_form_tooltip_attrRelease_releaseProxyTicket = 'Release proxy-granting ticket id to the application?';
 readonly services_form_tooltip_attrRelease_principleAttRepo_cached_timeUnit = 'Time unit of measure for the cache expiration policy.';
 readonly services_form_tooltip_attrRelease_principleAttRepo_cached_expiration = 'The cache expiration time. ';
 readonly services_form_tooltip_attrRelease_policies_returnAllowed = 'List of resolved attributes out of the available collection of attributes from the attribute repository. Select attributes that are allowed to be released to the application';
 readonly services_form_tooltip_attrRelease_policies_returnWsMapped = 'List of WS Federation Claims that will be mapped to the corresponding attribute from the attributes repository.';
 readonly services_form_tooltip_attrRelease_policies_restful = 'The URL of the REST endpoint that will be called to retrieve a list of attributes that will be released';
 readonly services_form_tooltip_attrRelease_policies_groovySaml = 'Location of a groovy script file that can be triggered by the server to determine the attributes to be released';
 readonly services_form_tooltip_attrRelease_consent_enabled = 'Control whether consent is active/inactive for this service.';
 readonly services_form_tooltip_attrRelease_consent_excludedAttributes = 'Exclude the indicated attributes from consent.';
 readonly services_form_tooltip_attrRelease_consent_includeOnlyAttributes = 'Force-include the indicated attributes in consent, provided attributes are resolved.';
 readonly services_form_tooltip_attrRelease_releaseAuthenticationAttributes = 'Determines whether this policy should exclude the authentication/protocol attributes for release. Authentication attributes are considered those that are not tied to a specific principal and define extra supplamentary metadata about the authentication event itself, such as the commencement date.';
 readonly services_form_tooltip_attrRelease_entityAttributeValues = 'List of attributes that are defined as part of the SP Metadata';
 readonly services_form_tooltip_attrRelease_entityAttribute = 'The EntityId for the SP providing the metadata attributes';
 readonly services_form_tooltip_attrRelease_entityAttributeFormat = 'Format description for the entity attribute.  This field is optional'
 readonly services_form_tooltip_sas_starttime = 'Determines the starting date/time from which service access is allowed.';
 readonly services_form_tooltip_sas_endtime = 'Determines the ending date/time from which service access is allowed.';
 readonly services_form_tooltip_sas_casEnabled = 'Decides whether access to this service is authorized by CAS.';
 readonly services_form_tooltip_sas_ssoEnabled = 'Decides whether this service is allowed to participate in SSO.';
 readonly services_form_tooltip_sas_requireAll = 'Decides whether all attributes AND corresponding values should be evaluated before access can be granted. If left unchecked, the first successful match on attribute name and value will allow access.';
 readonly services_form_tooltip_sas_requiredAttr = 'These are a set of attributes along with their values that decide whether access to this service can be granted. To configure, only provide values for attributes you care about, and leave the rest blank.';
 readonly services_form_tooltip_sas_unauthorizedredirecturl = 'URL to which CAS should direct in the event that service access is defined.';
 readonly services_form_tooltip_sas_remoteUrl = 'URL that CAS should contact to determine if access to this service should be granted.';
 readonly services_form_tooltip_sas_caseinsensitive = 'Indicates whether matching on required attribute values should be done in a case-insensitive manner.';
 readonly services_form_tooltip_sas_rejectedattributes = 'A Map of rejected principal attribute names along with the set of values for each attribute. These attributes MUST NOT be available to the authenticated Principal so that access may be granted. If none is defined, the check is entirely ignored.';
 readonly services_form_tooltip_sas_selecttype = 'Access strategy type';
 readonly services_form_tooltip_sas_authorizedCodes = 'Acceptable HTTP codes before granting access to this service.';
 readonly services_form_tooltip_sas_grouper = 'This access strategy attempts to locate Grouper groups for the CAS principal. The groups returned by ' +
    'Grouper are collected as CAS attributes and examined against the list of required attributes for service access.'
 readonly services_form_tooltip_sas_surrogate_enabled = 'Determines whether this service is allowed to use surrogate authentication.';
 readonly services_form_tooltip_sas_surrogate_ssoEnabled = 'Determines whether a Surrogate authentication should participate in SSO';
 readonly services_form_tooltip_sas_surrogate_requiredAttributes = 'List of attributes and values that must be present for the primary user in order to allow surrogate authentication';
 readonly services_form_tooltip_multiauth_providers = 'List of multifactor provider ids to assign to this service.';
 readonly services_form_tooltip_multiauth_failuremode = 'Decide what CAS should do in the event that a provider is not available.';
 readonly services_form_tooltip_multiauth_principalattribute_nametrigger = 'Principal attribute name to trigger MFA when accessing this service.';
 readonly services_form_tooltip_multiauth_principalattribute_valuetomatch = 'Principal attribute value to match in order to trigger MFA when accessing this service.';
 readonly services_form_tooltip_multiauth_bypassenabled = 'Bypass multifactor authentication for this service.';

 readonly services_form_tooltip_saml_header = 'SAML Client';
 readonly services_form_tooltip_saml_metadata_location = 'Metadata location for this particular service provider.';
 readonly services_form_tooltip_saml_metadata_maximumvalidity = 'Indicates how long should metadata be considered valid.';
 readonly services_form_tooltip_saml_authenticationcontextclass = 'The authentication context class that may be passed to the service provider. If ' +
    'none is defined here forcefully, metadata will be consulted instead.';
 readonly services_form_tooltip_saml_metadata_signaturepublickeylocation = 'Location of the metadata signing public key so its authenticity can be ' +
    'verified.';
 readonly services_form_tooltip_saml_signassertions = 'Sign Assertions';
 readonly services_form_tooltip_saml_signresponses = 'Sign Responses';
 readonly services_form_tooltip_saml_encryptassertions = 'Encrypt Assertions';
 readonly services_form_tooltip_saml_removeEmptyEntities = 'Controls whether to keep entities descriptors that contain no entity descriptors';
 readonly services_form_tooltip_saml_removeRoleless = 'Controls whether to keep entity descriptors that contain no roles';
 readonly services_form_tooltip_saml_metadata_pattern = 'Regex applied to entity ids in a metadata aggregate';
 readonly services_form_tooltip_saml_metadata_dir = 'Whether to include/exclude entity ids that match the filter pattern';
 readonly services_form_tooltip_saml_metadata_role = 'Whitelisted roles to keep in the metadata.';
 readonly services_form_tooltip_saml_requiredNameIdFormat = 'Force the indicated Name ID format in the final SAML response';
 readonly services_form_tooltip_saml_serviceProviderNameQualifier = 'Overwrite the SPNameQualifier attribute of the produced subject name id';
 readonly services_form_tooltip_saml_nameQualifier = 'Overwrite the NameQualifier attribute of the produced subject name id';
 readonly services_form_tooltip_saml_skipAssertioNameId = 'Whether generation of a name identifier should be skipped for assertions';
 readonly services_form_tooltip_saml_skipInResponseTo = 'Whether generation of the InResponseTo element should be skipped for subject confirmations';
 readonly services_form_tooltip_saml_skipNotOnOrAfter = 'Whether generation of the NotOnOrAfter element should be skipped for subject confirmations';
 readonly services_form_tooltip_saml_skipRecipient = 'Whether generation of the Recipient element should be skipped for subject confirmations';
 readonly services_form_tooltip_saml_skipNotBefore = 'Whether generation of the NotBefore element should be skipped for subject confirmations';
 readonly services_form_tooltip_saml_signingCredentialType = 'This setting controls the type of the signature block produced in the final SAML response for this application. The latter, being the default, encodes the signature in PEM format inside a X509Data block while the former encodes the signature based on the resolved public key under a DEREncodedKeyValue block.';

 readonly services_form_tooltip_oidc_signToken = 'Whether ID tokens should be signed.';
 readonly services_form_tooltip_oidc_encrypt = 'Whether ID tokens should be encrypted.';
 readonly services_form_tooltip_oidc_encryptEnc = 'Encryption content encoding algorithm to use with ID tokens.';
 readonly services_form_tooltip_oidc_encryptAlg = 'Encryption algorithm to use with ID tokens.';
 readonly services_form_tooltip_oidc_implicit = 'Indicates whether this service should support the implicit flow.';
 readonly services_form_tooltip_oidc_jwks = 'Location of the JSON web keystore to sign id tokens with. ';
 readonly services_form_tooltip_oidc_scopes = 'Scopes';
 readonly services_form_tooltip_oidc_user_defined_scopes = 'List any custom defined scopes that should be released.'
 readonly services_form_tooltip_oidc_subject_type = 'Type to use when generating principal identifiers. Default is public.'
 readonly services_form_tooltip_oidc_sector_identifier_uri = 'Host value of this URL is used as the sector identifier for the pairwise identifier calculation. If left undefined, the host value of the serviceId will be used instead.'

 readonly services_form_tooltip_expirationPolicy_expirationDate = 'The date on which the registration record is deemed expired. The expiration date may be specified in 2011-12-03T10:15:30, 09/24/1980 04:30 PM, 09/24/2014 6:30 AM, 09/24/2013 18:45, 09/24/2017 or 2017-10-25 formats.';
 readonly services_form_tooltip_expirationPolicy_deleteWhenExpired = 'When true, removes the application from the CAS service registry if and when expired. Otherwise the application record will be marked as disabled.';
 readonly services_form_tooltip_expirationPolicy_notifyWhenDeleted = 'Notifies contacts of the application via email or text, assuming valid contacts with email addresses or phone numbers are defined and CAS is configured to send email messages or SMS notifications. The notification is only sent if the application is expired and is about to be deleted from the registry.';

 readonly services_form_tooltip_attrRelease_principleAttRepo_cached_mergeStrategy = 'Decides how attributes that are retrieved from the cache ' +
    'should be merged into the existing attribute repository. Attributes may be replaced, ignored or contain multiple ' +
    'values as a result of the merge action.';
  readonly management_services_service_noAction = 'No further action is required.';

  readonly screen_unavailable_heading = 'The CAS management webapp is unavailable. ';
  readonly screen_unavailable_message = 'There was an error trying to complete your request. Please notify your support desk or try again.';

  readonly footer_links = 'Links to CAS Resources:';
  readonly footer_homePage = 'Home Page';
  readonly footer_wiki = 'Wiki';
  readonly footer_issueTracker = 'Issue Tracker';
  readonly footer_mailingLists = 'Mailing Lists';
  readonly footer_copyright = 'Copyright &copy; 2005 - 2017 Apereo, Inc. All rights reserved.';
  readonly footer_poweredBy = 'Powered by <a href=\'http://www.apereo.org/cas\'>Apereo Central Authentication Service {0}</a>';

  readonly services_form_label_canonicalizationMode = 'cononicalizationMode';
  readonly services_form_label_attrRelease_policies_patternMatching = 'Pattern Matching';
  readonly services_form_label_attrRelease_policies_releaseIncommon = 'InCommon';

  readonly services_form_label_wsfed_realm = 'Realm';
  readonly services_form_label_wsfed_appliesTo = 'appliesTo'
  readonly services_form_label_wsfed_wsFederationOnly = 'WS Federation only';

  readonly services_form_header_wsfedOptions = 'WS Federation';
  readonly services_form_header_nameId = 'Name ID Selection';
  readonly services_form_header_nameId_name = 'Name';
  readonly services_form_header_nameId_value = 'Value';

  readonly services_form_tooltip_uap_canonicalizationMode = 'canonicalizationMode tooltip';

  readonly services_form_tooltip_attrRelease_policies_patternMatching = 'In the event that an aggregate is defined containing multiple entity ids, the below attribute release policy may be used to release a collection of allowed attributes to entity ids grouped together by a regular expression pattern.';
  readonly services_form_tooltip_attrRelease_policies_releaseInCommon = 'Release the attribute bundles needed for InCommon\'s Research and Scholarship service providers?'

  readonly services_form_tooltip_wsfed_realm = 'The realm identifier of the application, identified via the wtrealm parameter. This needs to match the realm defined for the identity provider. By default it’s set to the realm defined for the CAS identity provider.';
  readonly services_form_tooltip_wsfed_appliesTo = 'Controls to whom security tokens apply. Defaults to the realm';

  readonly services_form_label_uap_canonicalizationMode = 'Canonicalization Mode';

}
