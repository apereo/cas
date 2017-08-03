import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {FormData} from "../../../domain/service-view-bean";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  AnonymousRegisteredServiceUsernameProvider,
  DefaultRegisteredServiceUsernameProvider,
  PrincipalAttributeRegisteredServiceUsernameProvider
} from "../../../domain/attribute-provider";
import {Data} from "../data";

@Component({
  selector: 'app-uidattrs',
  templateUrl: './uidattrs.component.html'
})
export class UidattrsComponent implements OnInit {

  service: AbstractRegisteredService;
  formData: FormData;
  selectOptions;
  type: String;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
    this.formData = data.formData;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    switch(this.service.usernameAttributeProvider["@class"]) {
      case "org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider" :
        this.type = 'default';
        break;
      case "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider" :
        this.type = 'attr';
        break;
      case "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider" :
        this.type = 'anon';
        break;
    }
  }

  changeType() {
    if (true) {
      switch(this.type) {
        case 'default' :
          this.service.usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();
          break;
        case 'attr' :
          this.service.usernameAttributeProvider = new PrincipalAttributeRegisteredServiceUsernameProvider();
          break;
        case 'anon' :
          this.service.usernameAttributeProvider = new AnonymousRegisteredServiceUsernameProvider();
          break;
      }
    }
  }

}
