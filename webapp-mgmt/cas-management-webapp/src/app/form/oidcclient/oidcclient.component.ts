import {Component, Input, OnInit} from '@angular/core';
import {OidcRegisteredService} from "../../../domain/oauth-service";
import {Messages} from "../../messages";
import {Data} from "../data";

@Component({
  selector: 'app-oidcclient',
  templateUrl: './oidcclient.component.html',
  styleUrls: ['./oidcclient.component.css']
})
export class OidcclientComponent implements OnInit {

  service: OidcRegisteredService;
  oidcScopes = [
    {name: "Profile", value: "profile"},
    {name: "Email", value: "email"},
    {name: "Address", value: "address"},
    {name: "Phone", value: "phone"},
    {name: "User Defined", value: "user_defined"}
  ];

  subjectType = [
    {name: "Public", value: "public"},
    {name: "Pairwise", value: "pairwise"}
  ];

  encryptAlgOptions = [
    "RSA1-5","RSA-OAEP","RSA-OAEP-256",
    "ECDH-ES","ECDH-ES+A128KW", "ECDH-ES+A192KW","ECDH-ES+A256KW",
    "A128KW","A192KW","A256KW",
    "A128GCMKW","A192GXMKW","A256GCMKW",
    "PBES2-HS256+A128KW","PBES2-HS384+A192KW","PBES2-HS512+A256KW"
  ]

  encodingAlgOptions = [
    "A128CBC-HS256","A192CBC-HS384","A256CBC-HS512",
    "A128GCM","A192GCM","A256GCM"
  ]

  constructor(public messages: Messages,
              public data: Data) {
    this.service = data.service as OidcRegisteredService;
  }

  ngOnInit() {
    if(!this.service.scopes) {
      this.service.scopes = [];
    }
  }

}
