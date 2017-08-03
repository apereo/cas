import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {SamlRegisteredService} from "../../../domain/saml-service";

@Component({
  selector: 'app-samlclient',
  templateUrl: './samlclient.component.html'
})
export class SamlclientComponent implements OnInit {

  @Input()
  service: SamlRegisteredService

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
