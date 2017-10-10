import {Component, OnInit} from '@angular/core';
import {Messages} from "../../messages";
import {SamlRegisteredService} from "../../../domain/saml-service";
import {Data} from "../data";

@Component({
  selector: 'app-samlclient',
  templateUrl: './samlclient.component.html'
})
export class SamlclientComponent implements OnInit {

  selectOptions;
  service: SamlRegisteredService;
  credentialType = ["BASIC","X509"];

  constructor(public messages: Messages,
              public data: Data) {
    this.selectOptions = data.selectOptions;
    this.service = data.service as SamlRegisteredService;
  }

  ngOnInit() {
  }

}
