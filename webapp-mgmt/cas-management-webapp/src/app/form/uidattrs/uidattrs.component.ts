import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {FormData} from '../../../domain/form-data';
import {
  AnonymousRegisteredServiceUsernameProvider,
  DefaultRegisteredServiceUsernameProvider,
  PrincipalAttributeRegisteredServiceUsernameProvider
} from '../../../domain/attribute-provider';
import {Data} from '../data';

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

  formData: FormData;
  type: Type;
  TYPE = Type;

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
    if (DefaultRegisteredServiceUsernameProvider.instanceOf(this.data.service.usernameAttributeProvider)) {
      this.type = Type.DEFAULT;
    } else if (PrincipalAttributeRegisteredServiceUsernameProvider.instanceOf(this.data.service.usernameAttributeProvider)) {
      this.type = Type.PRINCIPAL_ATTRIBUTE;
    } else if (AnonymousRegisteredServiceUsernameProvider.instanceOf(this.data.service.usernameAttributeProvider)) {
      this.type = Type.ANONYMOUS;
    }
  }

  changeType() {
      switch (+this.type) {
        case Type.DEFAULT :
          this.data.service.usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();
          break;
        case Type.PRINCIPAL_ATTRIBUTE :
          this.data.service.usernameAttributeProvider = new PrincipalAttributeRegisteredServiceUsernameProvider();
          break;
        case Type.ANONYMOUS :
          this.data.service.usernameAttributeProvider = new AnonymousRegisteredServiceUsernameProvider();
          break;
      }
  }

}
