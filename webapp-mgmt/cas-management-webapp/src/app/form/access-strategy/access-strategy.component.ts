import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {
  DefaultRegisteredServiceAccessStrategy, GrouperRegisteredServiceAccessStrategy,
  RemoteEndpointServiceAccessStrategy, TimeBasedRegisteredServiceAccessStrategy
} from "../../../domain/access-strategy";
import {FormData} from "../../../domain/service-view-bean";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Util} from "../../util/util";
import {Data} from "../data";

enum Type{
  DEFAULT,TIME,GROUPER,REMOTE
}

@Component({
  selector: 'app-access-strategy',
  templateUrl: './access-strategy.component.html',
})


export class AccessStrategyComponent implements OnInit {

  formData: FormData;
  service: AbstractRegisteredService;
  selectOptions;
  type: Type;
  TYPE = Type;
  types = [Type.DEFAULT,Type.TIME,Type.GROUPER,Type.REMOTE];

  constructor(public messages: Messages,
              private data: Data) {
    this.formData = data.formData;
    this.service = data.service;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (Util.isEmpty(this.service.accessStrategy.rejectedAttributes)) {
      this.service.accessStrategy.requiredAttributes = new Map();
    }

    this.formData.availableAttributes.forEach((item: any) => {
      this.service.accessStrategy.requiredAttributes[item] = this.service.accessStrategy.requiredAttributes[item] || [item];//this.textareaArrParse(dir, data.accessStrategy.requiredAttributes[item]);
    });

    if (RemoteEndpointServiceAccessStrategy.instanceOf(this.service.accessStrategy)) {
      this.type = Type.REMOTE;
    } else if (TimeBasedRegisteredServiceAccessStrategy.instanceOf(this.service.accessStrategy)) {
      this.type = Type.TIME;
    } else if (GrouperRegisteredServiceAccessStrategy.instanceOf(this.service.accessStrategy)) {
      this.type = Type.GROUPER;
    } else {
      this.type = Type.DEFAULT;
    }
  }

  changeType() {
    switch(+this.type) {
      case Type.DEFAULT :
        this.service.accessStrategy = new DefaultRegisteredServiceAccessStrategy(this.service.accessStrategy);
        break;
      case Type.REMOTE :
        this.service.accessStrategy = new RemoteEndpointServiceAccessStrategy(this.service.accessStrategy);
        break;
      case Type.TIME :
        console.log("Createing TimeBASEd");
        this.service.accessStrategy = new TimeBasedRegisteredServiceAccessStrategy(this.service.accessStrategy);
        break;
      case Type.GROUPER :
        this.service.accessStrategy = new GrouperRegisteredServiceAccessStrategy(this.service.accessStrategy);
        break;
      default:
    }
  }

}
