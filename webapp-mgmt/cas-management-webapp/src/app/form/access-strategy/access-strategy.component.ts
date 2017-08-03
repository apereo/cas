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

@Component({
  selector: 'app-access-strategy',
  templateUrl: './access-strategy.component.html',
})
export class AccessStrategyComponent implements OnInit {

  formData: FormData;
  service: AbstractRegisteredService;
  selectOptions;
  type: String;

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
      this.type = "REMOTE";
    } else if (TimeBasedRegisteredServiceAccessStrategy.instanceOf(this.service.accessStrategy)) {
      this.type = "TIME";
    } else if (GrouperRegisteredServiceAccessStrategy.instanceOf(this.service.accessStrategy)) {
      this.type = "GROUPER";
    } else {
      this.type = "DEFAULT";
    }
  }

  changeType() {
    switch(this.type) {
      case "DEFAULT" :
        this.service.accessStrategy = new DefaultRegisteredServiceAccessStrategy(this.service.accessStrategy);
        break;
      case "REMOTE" :
        this.service.accessStrategy = new RemoteEndpointServiceAccessStrategy(this.service.accessStrategy);
        break;
      case "TIME" :
        this.service.accessStrategy = new TimeBasedRegisteredServiceAccessStrategy(this.service.accessStrategy);
        break;
      case "GROUPER" :
        this.service.accessStrategy = new GrouperRegisteredServiceAccessStrategy(this.service.accessStrategy);
        break;
      default:
    }
  }

}
