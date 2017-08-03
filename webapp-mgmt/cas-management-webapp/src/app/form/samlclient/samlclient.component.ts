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

  service: SamlRegisteredService
  selectOptions;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service as SamlRegisteredService;
    this.selectOptions = data.selectOptions
  }

  ngOnInit() {
  }

}
