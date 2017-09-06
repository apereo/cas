import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {SamlRegisteredService} from "../../../domain/saml-service";
import {Data} from "../data";

@Component({
  selector: 'app-samlclient',
  templateUrl: './samlclient.component.html'
})
export class SamlclientComponent implements OnInit {

  selectOptions;
  service: SamlRegisteredService;

  constructor(public messages: Messages,
              public data: Data) {
    this.selectOptions = data.selectOptions;
    this.service = data.service as SamlRegisteredService;
  }

  ngOnInit() {
  }

}
