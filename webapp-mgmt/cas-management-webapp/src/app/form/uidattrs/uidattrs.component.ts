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

enum Type {
  DEFAULT,
  PRINCIPAL_ATTRIBUTE,
  ANONYMOUS
}

@Component({
  selector: 'app-uidattrs',
  templateUrl: './uidattrs.component.html'
})
export class UidattrsComponent implements OnInit {

  service: AbstractRegisteredService;
  formData: FormData;
  selectOptions;
  type: Type;
  TYPE = Type;
  canonicalizations = ["NONE","UPPER","LOWER"];

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
    this.formData = data.formData;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (DefaultRegisteredServiceUsernameProvider.instanceOf(this.service.usernameAttributeProvider)) {
      this.type = Type.DEFAULT;
    } else if (PrincipalAttributeRegisteredServiceUsernameProvider.instanceOf(this.service.usernameAttributeProvider)) {
      this.type = Type.PRINCIPAL_ATTRIBUTE;
    } else if (AnonymousRegisteredServiceUsernameProvider.instanceOf(this.service.usernameAttributeProvider)) {
      this.type = Type.ANONYMOUS;
    }
  }

  changeType() {
      switch(+this.type) {
        case Type.DEFAULT :
          this.service.usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();
          break;
        case Type.PRINCIPAL_ATTRIBUTE :
          this.service.usernameAttributeProvider = new PrincipalAttributeRegisteredServiceUsernameProvider();
          break;
        case Type.ANONYMOUS :
          this.service.usernameAttributeProvider = new AnonymousRegisteredServiceUsernameProvider();
          break;
      }
  }

}
