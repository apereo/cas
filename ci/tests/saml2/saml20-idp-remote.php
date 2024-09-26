<?php
$idp_entityId = getenv('IDP_ENTITYID');
$idp_signingCert = getenv('IDP_SIGNING_CERTIFICATE') ?? '';
$idp_encryptionCert = getenv('IDP_ENCRYPTION_CERTIFICATE') ?? '';

$singleSignOnServices = array(
    array(
        'Binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
        'Location' => 'https://localhost:8443/cas/idp/profile/SAML2/POST/SSO',
    ),
    array(
        'Binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST-SimpleSign',
        'Location' => 'https://localhost:8443/cas/idp/profile/SAML2/POST-SimpleSign/SSO',
    ),
    array(
        'Binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect',
        'Location' => 'https://localhost:8443/cas/idp/profile/SAML2/Redirect/SSO',
    ),
    array(
        'Binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:SOAP',
        'Location' => 'https://localhost:8443/cas/idp/profile/SAML2/SOAP/ECP',
    )
);

$disableRedirectBinding = getenv('DISABLE_REDIRECT_BINDING');
if ($disableRedirectBinding === 'true') {
    foreach ($singleSignOnServices as $key => $service) {
        if ($service['Binding'] === 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect') {
            unset($singleSignOnServices[$key]); 
        }
    }
}
$singleSignOnServices = array_values($singleSignOnServices); 
print_r($singleSignOnServices);

$metadata[$idp_entityId] = [
    'entityid' => $idp_entityId,
    // 'logouttype' => 'traditional',
    'contacts' => [],
    'metadata-set' => 'saml20-idp-remote',
    'SingleSignOnService' => $singleSignOnServices,
    'SingleLogoutService' => [
        [
            'Binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
            'Location' => 'https://localhost:8443/cas/idp/profile/SAML2/POST/SLO',
        ],
        [
            'Binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect',
            'Location' => 'https://localhost:8443/cas/idp/profile/SAML2/Redirect/SLO',
        ],
    ],
    'ArtifactResolutionService' => [],
    'NameIDFormats' => [
        'urn:mace:shibboleth:1.0:nameIdentifier',
        'urn:oasis:names:tc:SAML:2.0:nameid-format:transient',
    ],
    'keys' => [
        [
            'encryption' => false,
            'signing' => true,
            'type' => 'X509Certificate',
            'X509Certificate' => $idp_signingCert,
        ],
        [
            'encryption' => true,
            'signing' => false,
            'type' => 'X509Certificate',
            'X509Certificate' => $idp_encryptionCert
        ],
    ],
    'scope' => [
        'example.net',
    ],
];
