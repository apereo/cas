<?php
$config = [
 'admin' => array(
      'core:AdminPassword',
 ),
 'example-userpass' => array(
      'exampleauth:UserPass',
      'user1:password' => array(
          'email' => 'user1@example.com',
      ),
      'user2:password' => array(
          'email' => 'user2@example.com',
      ),
 ),
 'default-sp' => [
      'saml:SP',
      'privatekey' => 'saml.pem',
      'certificate' => 'saml.crt',
      'idp' => getenv('IDP_ENTITYID'),
      'IsPassive' => (getenv('SP_PASSIVE_AUTHN') === 'true'),
      'discoURL' => null
 ],
  'signed-sp' => [
       'saml:SP',
       'privatekey' => 'saml.pem',
       'certificate' => 'saml.crt',
       'idp' => getenv('IDP_ENTITYID'),
       'sign.authnrequest' => true,
       'IsPassive' => (getenv('SP_PASSIVE_AUTHN') === 'true'),
       'discoURL' => null
  ],
   'refeds-sp' => [
      'saml:SP',
      'privatekey' => 'saml.pem',
      'certificate' => 'saml.crt',
      'idp' => getenv('IDP_ENTITYID'),
      'sign.authnrequest' => true,
      'IsPassive' => (getenv('SP_PASSIVE_AUTHN') === 'true'),
      'discoURL' => null,
      'AuthnContextClassRef' => 'https://refeds.org/profile/mfa'
   ],
];
