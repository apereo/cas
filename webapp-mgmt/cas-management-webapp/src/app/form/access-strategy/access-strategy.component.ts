import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {
  DefaultRegisteredServiceAccessStrategy, GrouperRegisteredServiceAccessStrategy,
  RemoteEndpointServiceAccessStrategy, SurrogateRegisteredServiceAccessStrategy,
  TimeBasedRegisteredServiceAccessStrategy
} from '../../../domain/access-strategy';
import {FormData} from '../../../domain/form-data';
import {Util} from '../../util/util';
import {Data} from '../data';


enum Type {
  DEFAULT, TIME, GROUPER, REMOTE, SURROGATE
}

@Component({
  selector: 'app-access-strategy',
  templateUrl: './access-strategy.component.html',
})

export class AccessStrategyComponent implements OnInit {


  formData: FormData;
  type: Type;
  TYPE = Type;
  types = [Type.DEFAULT, Type.TIME, Type.GROUPER, Type.REMOTE, Type.SURROGATE];

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {

    const service = this.data.service;

    if (Util.isEmpty(service.accessStrategy.rejectedAttributes)) {
      service.accessStrategy.rejectedAttributes = new Map();
    }

    if (Util.isEmpty(service.accessStrategy.requiredAttributes)) {
      service.accessStrategy.requiredAttributes = new Map();
    }

    if (Util.isEmpty(service.accessStrategy.requiredAttributes)) {
      service.accessStrategy.requiredAttributes = new Map();
    }

    if (RemoteEndpointServiceAccessStrategy.instanceOf(service.accessStrategy)) {
      this.type = Type.REMOTE;
    } else if (TimeBasedRegisteredServiceAccessStrategy.instanceOf(service.accessStrategy)) {
      this.type = Type.TIME;
    } else if (GrouperRegisteredServiceAccessStrategy.instanceOf(service.accessStrategy)) {
      this.type = Type.GROUPER;
    } else if (SurrogateRegisteredServiceAccessStrategy.instanceOf(service.accessStrategy)) {
      this.type = Type.SURROGATE;
    } else {
      this.type = Type.DEFAULT;
    }
  }

  changeType() {
    switch (+this.type) {
      case Type.DEFAULT :
        this.data.service.accessStrategy = new DefaultRegisteredServiceAccessStrategy(this.data.service.accessStrategy);
        break;
      case Type.REMOTE :
        this.data.service.accessStrategy = new RemoteEndpointServiceAccessStrategy(this.data.service.accessStrategy);
        break;
      case Type.TIME :
        this.data.service.accessStrategy = new TimeBasedRegisteredServiceAccessStrategy(this.data.service.accessStrategy);
        break;
      case Type.GROUPER :
        this.data.service.accessStrategy = new GrouperRegisteredServiceAccessStrategy(this.data.service.accessStrategy);
        break;
      case Type.SURROGATE :
        this.data.service.accessStrategy = new SurrogateRegisteredServiceAccessStrategy(this.data.service.accessStrategy);
        break;
      default:
    }
  }
}
