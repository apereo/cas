import { Component, OnInit } from '@angular/core';
import {SamlRegisteredService} from "../../../../domain/saml-service";
import {Messages} from "../../../messages";
import {Data} from "../../data";

@Component({
  selector: 'app-security',
  templateUrl: './security.component.html',
  styleUrls: ['./security.component.css']
})
export class SecurityComponent implements OnInit {
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
