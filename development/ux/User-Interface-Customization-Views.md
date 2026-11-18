# Views

Source: https://apereo.github.io/cas/development/ux/User-Interface-Customization-Views.html

The views are found inside the CAS web application in the `WEB-INF\lib\cas-server-support-thymeleaf-<cas.version>.jar` in the templates folder. Add any views that require customization to the `src/main/resources/templates` folder in the CAS overlay project. Any files found in that module can be overridden by putting them in the same location under `src/main/resources` in the CAS overlay project. The Gradle build script for the overlay has tasks that help get resources from the CAS web application to the correct location in the CAS overlay.

## Templates

The following user interface templates are available and recognized by CAS for various modules and features:

| Category | Name | Full Path | Owner |
| --- | --- | --- | --- |
| `acct-mgmt` | `casAccountSignupView.html` | `/src/main/resources/templates/acct-mgmt/casAccountSignupView.html` | `Thymeleaf` |
| `acct-mgmt` | `casAccountSignupViewComplete.html` | `/src/main/resources/templates/acct-mgmt/casAccountSignupViewComplete.html` | `Thymeleaf` |
| `acct-mgmt` | `casAccountSignupViewCompleted.html` | `/src/main/resources/templates/acct-mgmt/casAccountSignupViewCompleted.html` | `Thymeleaf` |
| `acct-mgmt` | `casAccountSignupViewSentInfo.html` | `/src/main/resources/templates/acct-mgmt/casAccountSignupViewSentInfo.html` | `Thymeleaf` |
| `acct` | `casMyAccountProfile.html` | `/src/main/resources/templates/acct/casMyAccountProfile.html` | `Thymeleaf` |
| `adaptive-authn` | `casRiskAuthenticationBlockedView.html` | `/src/main/resources/templates/adaptive-authn/casRiskAuthenticationBlockedView.html` | `Thymeleaf` |
| `adaptive-authn` | `casRiskAuthenticationVerifiedView.html` | `/src/main/resources/templates/adaptive-authn/casRiskAuthenticationVerifiedView.html` | `Thymeleaf` |
| `admin` | `casAdminLoginView.html` | `/src/main/resources/templates/admin/casAdminLoginView.html` | `Thymeleaf` |
| `aup` | `casAcceptableUsagePolicyView.html` | `/src/main/resources/templates/aup/casAcceptableUsagePolicyView.html` | `Thymeleaf` |
| `consent` | `casConsentView.html` | `/src/main/resources/templates/consent/casConsentView.html` | `Thymeleaf` |
| `delegated-authn` | `casDelegatedAuthnErrorView.html` | `/src/main/resources/templates/delegated-authn/casDelegatedAuthnErrorView.html` | `Thymeleaf` |
| `delegated-authn` | `casDelegatedAuthnSelectionView.html` | `/src/main/resources/templates/delegated-authn/casDelegatedAuthnSelectionView.html` | `Thymeleaf` |
| `delegated-authn` | `casDelegatedAuthnStopWebflow.html` | `/src/main/resources/templates/delegated-authn/casDelegatedAuthnStopWebflow.html` | `Thymeleaf` |
| `delegated-authn` | `casDynamicDiscoveryView.html` | `/src/main/resources/templates/delegated-authn/casDynamicDiscoveryView.html` | `Thymeleaf` |
| `error` | `error.html` | `/src/main/resources/templates/error.html` | `Thymeleaf` |
| `error` | `400.html` | `/src/main/resources/templates/error/400.html` | `Thymeleaf` |
| `error` | `401.html` | `/src/main/resources/templates/error/401.html` | `Thymeleaf` |
| `error` | `403.html` | `/src/main/resources/templates/error/403.html` | `Thymeleaf` |
| `error` | `404.html` | `/src/main/resources/templates/error/404.html` | `Thymeleaf` |
| `error` | `405.html` | `/src/main/resources/templates/error/405.html` | `Thymeleaf` |
| `error` | `422.html` | `/src/main/resources/templates/error/422.html` | `Thymeleaf` |
| `error` | `423.html` | `/src/main/resources/templates/error/423.html` | `Thymeleaf` |
| `error` | `casServiceErrorView.html` | `/src/main/resources/templates/error/casServiceErrorView.html` | `Thymeleaf` |
| `error` | `casUnauthorizedServiceRedirectView.html` | `/src/main/resources/templates/error/casUnauthorizedServiceRedirectView.html` | `Thymeleaf` |
| `error` | `casUnknownTenantView.html` | `/src/main/resources/templates/error/casUnknownTenantView.html` | `Thymeleaf` |
| `error` | `casWebflowConfigErrorView.html` | `/src/main/resources/templates/error/casWebflowConfigErrorView.html` | `Thymeleaf` |
| `forgot-username` | `casForgotUsernameSendInfoView.html` | `/src/main/resources/templates/forgot-username/casForgotUsernameSendInfoView.html` | `Thymeleaf` |
| `forgot-username` | `casForgotUsernameSentInfoView.html` | `/src/main/resources/templates/forgot-username/casForgotUsernameSentInfoView.html` | `Thymeleaf` |
| `fragments` | `accountflow.html` | `/src/main/resources/templates/fragments/accountflow.html` | `Thymeleaf` |
| `fragments` | `accountprofileaccesstokens.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileaccesstokens.html` | `Thymeleaf` |
| `fragments` | `accountprofileapplications.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileapplications.html` | `Thymeleaf` |
| `fragments` | `accountprofileattributes.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileattributes.html` | `Thymeleaf` |
| `fragments` | `accountprofileauditlog.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileauditlog.html` | `Thymeleaf` |
| `fragments` | `accountprofileaup.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileaup.html` | `Thymeleaf` |
| `fragments` | `accountprofileconsent.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileconsent.html` | `Thymeleaf` |
| `fragments` | `accountprofilecontactmethods.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofilecontactmethods.html` | `Thymeleaf` |
| `fragments` | `accountprofilemfadevices.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofilemfadevices.html` | `Thymeleaf` |
| `fragments` | `accountprofilenavigation.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofilenavigation.html` | `Thymeleaf` |
| `fragments` | `accountprofileoverview.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileoverview.html` | `Thymeleaf` |
| `fragments` | `accountprofileqrdevices.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofileqrdevices.html` | `Thymeleaf` |
| `fragments` | `accountprofilesecurityquestions.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofilesecurityquestions.html` | `Thymeleaf` |
| `fragments` | `accountprofilesessions.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofilesessions.html` | `Thymeleaf` |
| `fragments` | `accountprofilesigninactivity.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofilesigninactivity.html` | `Thymeleaf` |
| `fragments` | `accountprofilesurrogate.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofilesurrogate.html` | `Thymeleaf` |
| `fragments` | `accountprofiletrusteddevices.html` | `/src/main/resources/templates/fragments/accountprofile/accountprofiletrusteddevices.html` | `Thymeleaf` |
| `fragments` | `consent.html` | `/src/main/resources/templates/fragments/consent.html` | `Thymeleaf` |
| `fragments` | `deviceName.html` | `/src/main/resources/templates/fragments/deviceName.html` | `Thymeleaf` |
| `fragments` | `footer.html` | `/src/main/resources/templates/fragments/footer.html` | `Thymeleaf` |
| `fragments` | `gauthConfirmation.html` | `/src/main/resources/templates/fragments/gauthConfirmation.html` | `Thymeleaf` |
| `fragments` | `googleanalytics.html` | `/src/main/resources/templates/fragments/googleanalytics.html` | `Thymeleaf` |
| `fragments` | `header.html` | `/src/main/resources/templates/fragments/header.html` | `Thymeleaf` |
| `fragments` | `includes.html` | `/src/main/resources/templates/fragments/includes.html` | `Thymeleaf` |
| `fragments` | `loginProviders.html` | `/src/main/resources/templates/fragments/loginProviders.html` | `Thymeleaf` |
| `fragments` | `loginSidePanel.html` | `/src/main/resources/templates/fragments/loginSidePanel.html` | `Thymeleaf` |
| `fragments` | `logindrawer.html` | `/src/main/resources/templates/fragments/logindrawer.html` | `Thymeleaf` |
| `fragments` | `loginform.html` | `/src/main/resources/templates/fragments/loginform.html` | `Thymeleaf` |
| `fragments` | `loginsidebar.html` | `/src/main/resources/templates/fragments/loginsidebar.html` | `Thymeleaf` |
| `fragments` | `accessstrategytab.html` | `/src/main/resources/templates/fragments/palantir/accessstrategytab.html` | `Thymeleaf` |
| `fragments` | `authenticationtab.html` | `/src/main/resources/templates/fragments/palantir/authenticationtab.html` | `Thymeleaf` |
| `fragments` | `clustertab.html` | `/src/main/resources/templates/fragments/palantir/clustertab.html` | `Thymeleaf` |
| `fragments` | `configurationtab.html` | `/src/main/resources/templates/fragments/palantir/configurationtab.html` | `Thymeleaf` |
| `fragments` | `consenttab.html` | `/src/main/resources/templates/fragments/palantir/consenttab.html` | `Thymeleaf` |
| `fragments` | `dashboardtabs.html` | `/src/main/resources/templates/fragments/palantir/dashboardtabs.html` | `Thymeleaf` |
| `fragments` | `loggerstab.html` | `/src/main/resources/templates/fragments/palantir/loggerstab.html` | `Thymeleaf` |
| `fragments` | `mfatab.html` | `/src/main/resources/templates/fragments/palantir/mfatab.html` | `Thymeleaf` |
| `fragments` | `multitenancytab.html` | `/src/main/resources/templates/fragments/palantir/multitenancytab.html` | `Thymeleaf` |
| `fragments` | `navigationsidebar.html` | `/src/main/resources/templates/fragments/palantir/navigationsidebar.html` | `Thymeleaf` |
| `fragments` | `persondirectorytab.html` | `/src/main/resources/templates/fragments/palantir/persondirectorytab.html` | `Thymeleaf` |
| `fragments` | `protocolstab.html` | `/src/main/resources/templates/fragments/palantir/protocolstab.html` | `Thymeleaf` |
| `fragments` | `servicestab.html` | `/src/main/resources/templates/fragments/palantir/servicestab.html` | `Thymeleaf` |
| `fragments` | `settingspanel.html` | `/src/main/resources/templates/fragments/palantir/settingspanel.html` | `Thymeleaf` |
| `fragments` | `ssosessionstab.html` | `/src/main/resources/templates/fragments/palantir/ssosessionstab.html` | `Thymeleaf` |
| `fragments` | `systemtab.html` | `/src/main/resources/templates/fragments/palantir/systemtab.html` | `Thymeleaf` |
| `fragments` | `taskstab.html` | `/src/main/resources/templates/fragments/palantir/taskstab.html` | `Thymeleaf` |
| `fragments` | `throttlestab.html` | `/src/main/resources/templates/fragments/palantir/throttlestab.html` | `Thymeleaf` |
| `fragments` | `ticketstab.html` | `/src/main/resources/templates/fragments/palantir/ticketstab.html` | `Thymeleaf` |
| `fragments` | `userstab.html` | `/src/main/resources/templates/fragments/palantir/userstab.html` | `Thymeleaf` |
| `fragments` | `pmlinks.html` | `/src/main/resources/templates/fragments/pmlinks.html` | `Thymeleaf` |
| `fragments` | `pwdupdateform.html` | `/src/main/resources/templates/fragments/pwdupdateform.html` | `Thymeleaf` |
| `fragments` | `qrAuthentication.html` | `/src/main/resources/templates/fragments/qrAuthentication.html` | `Thymeleaf` |
| `fragments` | `recaptcha.html` | `/src/main/resources/templates/fragments/recaptcha.html` | `Thymeleaf` |
| `fragments` | `scripts.html` | `/src/main/resources/templates/fragments/scripts.html` | `Thymeleaf` |
| `fragments` | `serviceui.html` | `/src/main/resources/templates/fragments/serviceui.html` | `Thymeleaf` |
| `fragments` | `submitbutton.html` | `/src/main/resources/templates/fragments/submitbutton.html` | `Thymeleaf` |
| `fragments` | `switchbutton.html` | `/src/main/resources/templates/fragments/switchbutton.html` | `Thymeleaf` |
| `fragments` | `themebuilder.html` | `/src/main/resources/templates/fragments/themebuilder.html` | `Thymeleaf` |
| `fragments` | `unlockaccount.html` | `/src/main/resources/templates/fragments/unlockaccount.html` | `Thymeleaf` |
| `fragments` | `webAuthnLogin.html` | `/src/main/resources/templates/fragments/webAuthnLogin.html` | `Thymeleaf` |
| `fragments` | `webAuthnMessages.html` | `/src/main/resources/templates/fragments/webAuthnMessages.html` | `Thymeleaf` |
| `fragments` | `webAuthnPasskey.html` | `/src/main/resources/templates/fragments/webAuthnPasskey.html` | `Thymeleaf` |
| `gauth` | `casGoogleAuthenticatorConfirmRegistrationView.html` | `/src/main/resources/templates/gauth/casGoogleAuthenticatorConfirmRegistrationView.html` | `Thymeleaf` |
| `gauth` | `casGoogleAuthenticatorLoginView.html` | `/src/main/resources/templates/gauth/casGoogleAuthenticatorLoginView.html` | `Thymeleaf` |
| `gauth` | `casGoogleAuthenticatorRegistrationRequiredView.html` | `/src/main/resources/templates/gauth/casGoogleAuthenticatorRegistrationRequiredView.html` | `Thymeleaf` |
| `gauth` | `casGoogleAuthenticatorRegistrationView.html` | `/src/main/resources/templates/gauth/casGoogleAuthenticatorRegistrationView.html` | `Thymeleaf` |
| `gauth` | `casGoogleAuthenticatorTokenAuthorizationErrorView.html` | `/src/main/resources/templates/gauth/casGoogleAuthenticatorTokenAuthorizationErrorView.html` | `Thymeleaf` |
| `gua` | `casGuaDisplayUserGraphicsView.html` | `/src/main/resources/templates/gua/casGuaDisplayUserGraphicsView.html` | `Thymeleaf` |
| `gua` | `casGuaGetUserIdView.html` | `/src/main/resources/templates/gua/casGuaGetUserIdView.html` | `Thymeleaf` |
| `interrupt` | `casInterruptView.html` | `/src/main/resources/templates/interrupt/casInterruptView.html` | `Thymeleaf` |
| `inwebo` | `casInweboCheckResultView.html` | `/src/main/resources/templates/inwebo/casInweboCheckResultView.html` | `Thymeleaf` |
| `inwebo` | `casInweboErrorView.html` | `/src/main/resources/templates/inwebo/casInweboErrorView.html` | `Thymeleaf` |
| `inwebo` | `casInweboMAAuthnView.html` | `/src/main/resources/templates/inwebo/casInweboMAAuthnView.html` | `Thymeleaf` |
| `inwebo` | `casInweboSelectAuthnView.html` | `/src/main/resources/templates/inwebo/casInweboSelectAuthnView.html` | `Thymeleaf` |
| `inwebo` | `casInweboVAAuthnView.html` | `/src/main/resources/templates/inwebo/casInweboVAAuthnView.html` | `Thymeleaf` |
| `layout` | `layout.html` | `/src/main/resources/templates/layout.html` | `Thymeleaf` |
| `login-error` | `casAccountDisabledView.html` | `/src/main/resources/templates/login-error/casAccountDisabledView.html` | `Thymeleaf` |
| `login-error` | `casAccountLockedView.html` | `/src/main/resources/templates/login-error/casAccountLockedView.html` | `Thymeleaf` |
| `login-error` | `casAccountUnlockedView.html` | `/src/main/resources/templates/login-error/casAccountUnlockedView.html` | `Thymeleaf` |
| `login-error` | `casAuthenticationBlockedView.html` | `/src/main/resources/templates/login-error/casAuthenticationBlockedView.html` | `Thymeleaf` |
| `login-error` | `casBadHoursView.html` | `/src/main/resources/templates/login-error/casBadHoursView.html` | `Thymeleaf` |
| `login-error` | `casBadWorkstationView.html` | `/src/main/resources/templates/login-error/casBadWorkstationView.html` | `Thymeleaf` |
| `login-error` | `casExpiredPassView.html` | `/src/main/resources/templates/login-error/casExpiredPassView.html` | `Thymeleaf` |
| `login-error` | `casMustChangePassView.html` | `/src/main/resources/templates/login-error/casMustChangePassView.html` | `Thymeleaf` |
| `login` | `casConfirmView.html` | `/src/main/resources/templates/login/casConfirmView.html` | `Thymeleaf` |
| `login` | `casGenericSuccessView.html` | `/src/main/resources/templates/login/casGenericSuccessView.html` | `Thymeleaf` |
| `login` | `casLoginMessageView.html` | `/src/main/resources/templates/login/casLoginMessageView.html` | `Thymeleaf` |
| `login` | `casLoginView.html` | `/src/main/resources/templates/login/casLoginView.html` | `Thymeleaf` |
| `logout` | `casConfirmLogoutView.html` | `/src/main/resources/templates/logout/casConfirmLogoutView.html` | `Thymeleaf` |
| `logout` | `casLogoutView.html` | `/src/main/resources/templates/logout/casLogoutView.html` | `Thymeleaf` |
| `logout` | `casPropagateLogoutView.html` | `/src/main/resources/templates/logout/casPropagateLogoutView.html` | `Thymeleaf` |
| `mfa-trusted-devices` | `casMfaRegisterDeviceView.html` | `/src/main/resources/templates/mfa-trusted-devices/casMfaRegisterDeviceView.html` | `Thymeleaf` |
| `mfa` | `casCompositeMfaProviderSelectionView.html` | `/src/main/resources/templates/mfa/casCompositeMfaProviderSelectionView.html` | `Thymeleaf` |
| `mfa` | `casMfaDeniedView.html` | `/src/main/resources/templates/mfa/casMfaDeniedView.html` | `Thymeleaf` |
| `mfa` | `casMfaUnavailableView.html` | `/src/main/resources/templates/mfa/casMfaUnavailableView.html` | `Thymeleaf` |
| `palantir` | `casPalantirDashboardView.html` | `/src/main/resources/templates/palantir/casPalantirDashboardView.html` | `Thymeleaf` |
| `password-reset` | `casPasswordUpdateSuccessView.html` | `/src/main/resources/templates/password-reset/casPasswordUpdateSuccessView.html` | `Thymeleaf` |
| `password-reset` | `casResetPasswordErrorView.html` | `/src/main/resources/templates/password-reset/casResetPasswordErrorView.html` | `Thymeleaf` |
| `password-reset` | `casResetPasswordSendInstructionsView.html` | `/src/main/resources/templates/password-reset/casResetPasswordSendInstructionsView.html` | `Thymeleaf` |
| `password-reset` | `casResetPasswordSentInstructionsView.html` | `/src/main/resources/templates/password-reset/casResetPasswordSentInstructionsView.html` | `Thymeleaf` |
| `password-reset` | `casResetPasswordVerifyQuestionsView.html` | `/src/main/resources/templates/password-reset/casResetPasswordVerifyQuestionsView.html` | `Thymeleaf` |
| `password-reset` | `casWeakPasswordDetectedView.html` | `/src/main/resources/templates/password-reset/casWeakPasswordDetectedView.html` | `Thymeleaf` |
| `passwordless` | `casPasswordlessDisplayView.html` | `/src/main/resources/templates/passwordless/casPasswordlessDisplayView.html` | `Thymeleaf` |
| `passwordless` | `casPasswordlessGetUserIdView.html` | `/src/main/resources/templates/passwordless/casPasswordlessGetUserIdView.html` | `Thymeleaf` |
| `passwordless` | `casPasswordlessSelectionMenuView.html` | `/src/main/resources/templates/passwordless/casPasswordlessSelectionMenuView.html` | `Thymeleaf` |
| `protocol` | `casProxyFailureView.mustache` | `/src/main/resources/templates/protocol/2.0/casProxyFailureView.mustache` | `Mustache` |
| `protocol` | `casProxySuccessView.mustache` | `/src/main/resources/templates/protocol/2.0/casProxySuccessView.mustache` | `Mustache` |
| `protocol` | `casServiceValidationFailure.mustache` | `/src/main/resources/templates/protocol/2.0/casServiceValidationFailure.mustache` | `Mustache` |
| `protocol` | `casServiceValidationSuccess.mustache` | `/src/main/resources/templates/protocol/2.0/casServiceValidationSuccess.mustache` | `Mustache` |
| `protocol` | `casServiceValidationFailure.mustache` | `/src/main/resources/templates/protocol/3.0/casServiceValidationFailure.mustache` | `Mustache` |
| `protocol` | `casServiceValidationSuccess.mustache` | `/src/main/resources/templates/protocol/3.0/casServiceValidationSuccess.mustache` | `Mustache` |
| `protocol` | `casPostResponseView.html` | `/src/main/resources/templates/protocol/casPostResponseView.html` | `Thymeleaf` |
| `protocol` | `confirm.html` | `/src/main/resources/templates/protocol/oauth/confirm.html` | `Thymeleaf` |
| `protocol` | `deviceCodeApproval.html` | `/src/main/resources/templates/protocol/oauth/deviceCodeApproval.html` | `Thymeleaf` |
| `protocol` | `deviceCodeApproved.html` | `/src/main/resources/templates/protocol/oauth/deviceCodeApproved.html` | `Thymeleaf` |
| `protocol` | `sessionStaleMismatchError.html` | `/src/main/resources/templates/protocol/oauth/sessionStaleMismatchError.html` | `Thymeleaf` |
| `protocol` | `cibaVerification.html` | `/src/main/resources/templates/protocol/oidc/cibaVerification.html` | `Thymeleaf` |
| `protocol` | `confirm.html` | `/src/main/resources/templates/protocol/oidc/confirm.html` | `Thymeleaf` |
| `radius` | `casRadiusLoginView.html` | `/src/main/resources/templates/radius/casRadiusLoginView.html` | `Thymeleaf` |
| `saml2-discovery` | `casSamlIdPDiscoveryView.html` | `/src/main/resources/templates/saml2-discovery/casSamlIdPDiscoveryView.html` | `Thymeleaf` |
| `saml2-idp` | `casSamlIdPErrorView.html` | `/src/main/resources/templates/saml2-idp/casSamlIdPErrorView.html` | `Thymeleaf` |
| `simple-mfa` | `casSimpleMfaLoginView.html` | `/src/main/resources/templates/simple-mfa/casSimpleMfaLoginView.html` | `Thymeleaf` |
| `simple-mfa` | `casSimpleMfaRegisterRecipientsView.html` | `/src/main/resources/templates/simple-mfa/casSimpleMfaRegisterRecipientsView.html` | `Thymeleaf` |
| `simple-mfa` | `casSimpleMfaSelectRecipientsView.html` | `/src/main/resources/templates/simple-mfa/casSimpleMfaSelectRecipientsView.html` | `Thymeleaf` |
| `simple-mfa` | `casSimpleMfaUpdateEmailRegistrationView.html` | `/src/main/resources/templates/simple-mfa/casSimpleMfaUpdateEmailRegistrationView.html` | `Thymeleaf` |
| `storage` | `casBrowserStorageReadView.html` | `/src/main/resources/templates/storage/casBrowserStorageReadView.html` | `Thymeleaf` |
| `storage` | `casBrowserStorageWriteView.html` | `/src/main/resources/templates/storage/casBrowserStorageWriteView.html` | `Thymeleaf` |
| `surrogate` | `casSurrogateAuthnListView.html` | `/src/main/resources/templates/surrogate/casSurrogateAuthnListView.html` | `Thymeleaf` |
| `surrogate` | `casSurrogateAuthnWildcardView.html` | `/src/main/resources/templates/surrogate/casSurrogateAuthnWildcardView.html` | `Thymeleaf` |
| `twilio-mfa` | `casTwilioMfaLoginView.html` | `/src/main/resources/templates/twilio-mfa/casTwilioMfaLoginView.html` | `Thymeleaf` |
| `webauthn` | `casWebAuthnLoginView.html` | `/src/main/resources/templates/webauthn/casWebAuthnLoginView.html` | `Thymeleaf` |
| `webauthn` | `casWebAuthnPasskeyUpgradeView.html` | `/src/main/resources/templates/webauthn/casWebAuthnPasskeyUpgradeView.html` | `Thymeleaf` |
| `webauthn` | `casWebAuthnQRCodeVerifyDoneView.html` | `/src/main/resources/templates/webauthn/casWebAuthnQRCodeVerifyDoneView.html` | `Thymeleaf` |
| `webauthn` | `casWebAuthnQRCodeVerifyView.html` | `/src/main/resources/templates/webauthn/casWebAuthnQRCodeVerifyView.html` | `Thymeleaf` |
| `webauthn` | `casWebAuthnRegistrationView.html` | `/src/main/resources/templates/webauthn/casWebAuthnRegistrationView.html` | `Thymeleaf` |
| `wsfed` | `casWsFedStopWebflow.html` | `/src/main/resources/templates/wsfed/casWsFedStopWebflow.html` | `Thymeleaf` |
| `yubikey` | `casYubiKeyLoginView.html` | `/src/main/resources/templates/yubikey/casYubiKeyLoginView.html` | `Thymeleaf` |
| `yubikey` | `casYubiKeyRegistrationView.html` | `/src/main/resources/templates/yubikey/casYubiKeyRegistrationView.html` | `Thymeleaf` |

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.view.template-prefixes` (Optional; type `List<String>`): Comma separated paths to where CAS templates may be found. Example might be `classpath:templates,file:/templates`.

## Warning Before Accessing Application

CAS has the ability to warn the user before being redirected to the service. This allows users to be made aware whenever an application uses CAS to log them in. (If they don’t elect the warning, they may not see any CAS screen when accessing an application that successfully relies upon an existing CAS single sign-on session.) Some CAS adopters remove the ‘warn’ checkbox in the CAS login view and don’t offer this interstitial advisement that single sign-on is happening.

```html
...
<input id="warn"
   name="warn"
   value="true"
   th:accesskey="#{screen.welcome.label.warn.accesskey}"
   type="checkbox" />
<label for="warn" th:utext="#{screen.welcome.label.warn}"/>
...
```
