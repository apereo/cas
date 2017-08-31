import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";
import {SamlRegisteredService} from "../../../domain/saml-service";

@Component({
  selector: 'app-serviceid',
  templateUrl: './serviceid.component.html'
})
export class ServiceidComponent implements OnInit {

  isSaml: boolean;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    this.isSaml = SamlRegisteredService.instanceOf(this.data.service);
  }

  placeholder() {
    if (SamlRegisteredService.instanceOf(this.data.service)) {
      return this.messages.services_form_label_entityId;
    } else {
      return this.messages.services_form_label_serviceId;
    }
  }

  tooltip() {
    if (SamlRegisteredService.instanceOf(this.data.service)) {
      return this.messages.services_form_tooltip_entityId;
    } else {
      return this.messages.services_form_tooltip_serviceId;
    }
  }
}
