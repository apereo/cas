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
      'idp' => $_ENV['IDP_ENTITYID'],
      'IsPassive' => ($_ENV['SP_PASSIVE_AUTHN'] === 'true')
 ],
  'signed-sp' => [
       'saml:SP',
       'privatekey' => 'saml.pem',
       'certificate' => 'saml.crt',
       'idp' => $_ENV['IDP_ENTITYID'],
       'sign.authnrequest' => true,
       'IsPassive' => ($_ENV['SP_PASSIVE_AUTHN'] === 'true')
  ]
];
