/**
 * Created by tschmidt on 2/22/17.
 */

import {EventEmitter, Injectable} from "@angular/core";
import {AbstractRegisteredService} from "../../domain/registered-service";
import {FormData} from "../../domain/service-view-bean";
import {FormComponent} from "./form.component";

@Injectable()
export class Data {
  service: AbstractRegisteredService;
  formData: FormData;
  submitted: boolean;
  save: EventEmitter<void> = new EventEmitter<void>();
  typeChange: EventEmitter<void> = new EventEmitter<void>();
  form: FormComponent;

  roles = [
    {id: 1, text: "guest"},
    {id: 2, text: "user"},
    {id: 3, text: "customer"},
    {id: 4, text: "admin"}
  ];

  selectOptions = {
    serviceTypeList: [
      {name: "CAS Client", value: "cas"},
      {name: "OAuth2 Client", value: "oauth"},
      {name: "OAuth Callback Authorize", value: "oauth_callback_authz"},
      {name: "SAML2 Service Provider", value: "saml"},
      {name: "OpenID Connect Client", value: "oidc"},
      {name: "WS Federation", value: "wsfed"}
    ],

    samlRoleList: [
      {name: "SPSSODescriptor", value: "SPSSODescriptor"},
      {name: "IDPSSODescriptor", value: "IDPSSODescriptor"}
    ],
    samlDirectionList: [
      {name: "INCLUDE", value: "INCLUDE"},
      {name: "EXCLUDE", value: "EXCLUDE"}
    ],
    nameIdList : [
      {name: "BASIC", value: "Basic"},
      {name: "URI", value: "URI"},
      {name: "UNSPECIFIED", value: "UNSPECIFIED"}
    ],
    wsfedClaimList: [
      {name: "EMAIL_ADDRESS_2005", value: "EMAIL_ADDRESS_2006"},
      {name: "EMAIL_ADDRESS", value: "EMAIL_ADDRESS"},
      {name: "GIVEN_NAME", value: "GIVEN_NAME"},
      {name: "NAME", value: "NAME"},
      {name: "USER_PRINCIPAL_NAME_2005", value: "USER_PRINCIPAL_NAME_2006"},
      {name: "USER_PRINCIPAL_NAME", value: "USER_PRINCIPAL_NAME"},
      {name: "COMMON_NAME", value: "COMMON_NAME"},
      {name: "GROUP", value: "GROUP"},
      {name: "MS_ROLE", value: "MS_ROLE"},
      {name: "ROLE", value: "ROLE"},
      {name: "SURNAME", value: "SURNAME"},
      {name: "PRIVATE_ID", value: "PRIVATE_ID"},
      {name: "NAME_IDENTIFIER", value: "NAME_IDENTIFIER"},
      {name: "AUTHENTICATION_METHOD", value: "AUTHENTICATION_METHOD"},
      {name: "DENY_ONLY_GROUP_SID", value: "DENY_ONLY_GROUP_SID"},
      {name: "DENY_ONLY_PRIMARY_SID", value: "DENY_ONLY_PRIMARY_SID"},
      {name: "DENY_ONLY_PRIMARY_GROUP_SID", value: "DENY_ONLY_PRIMARY_GROUP_SID"},
      {name: "GROUP_SID", value: "GROUP_SID"},
      {name: "PRIMARY_GROUP_SID", value: "PRIMARY_GROUP_SID"},
      {name: "PRIMARY_SID", value: "PRIMARY_SID"},
      {name: "WINDOWS_ACCOUNT_NAME", value: "WINDOWS_ACCOUNT_NAME"},
      {name: "PUID", value: "PUID"}
    ]
  };
}


