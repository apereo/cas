import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {
  DefaultRegisteredServiceAccessStrategy, GrouperRegisteredServiceAccessStrategy,
  RegisteredServiceAccessStrategy,
  RemoteEndpointServiceAccessStrategy, TimeBasedRegisteredServiceAccessStrategy
} from "../../../domain/access-strategy";
import {FormData} from "../../../domain/service-view-bean";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-access-strategy',
  templateUrl: './access-strategy.component.html',
})
export class AccessStrategyComponent implements OnInit {

  @Input()
  formData: FormData;

  @Input()
  service: AbstractRegisteredService;

  @Input()
  selectOptions;

  type: String;

  constructor(public messages: Messages) { }

  ngOnInit() {
    if (!this.service.accessStrategy.requiredAttributes || Object.keys(this.service.accessStrategy.requiredAttributes).length == 0) {
      this.service.accessStrategy.requiredAttributes = new Map();
    }
    this.formData.availableAttributes.forEach((item: any) => {
      this.service.accessStrategy.requiredAttributes[item] = this.service.accessStrategy.requiredAttributes[item] || [item];//this.textareaArrParse(dir, data.accessStrategy.requiredAttributes[item]);
    });
    switch(this.service.accessStrategy["@class"]) {
      case "org.apereo.cas.services.RemoteEndpointServiceAccessStrategy" :
        this.type = "REMOTE";
        break;
      case "org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy" :
        this.type = "TIME";
        break;
      case "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy" :
        this.type = "GROUPER";
        break;
      default :
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
