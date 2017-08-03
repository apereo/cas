import {Component, OnInit, Input} from '@angular/core';
import {FormData } from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  DenyAllAttributeReleasePolicy, GroovyScriptAttributeReleasePolicy, ReturnAllAttributeReleasePolicy,
  ReturnAllowedAttributeReleasePolicy,
  ReturnMappedAttributeReleasePolicy, ScriptedRegisteredServiceAttributeReleasePolicy
} from "../../../domain/attribute-release";
import {Data} from "../data";

@Component({
  selector: 'app-attribute-release-policies',
  templateUrl: './attribute-release-policies.component.html',
  styleUrls: ['./attribute-release-policies.component.css']
})
export class AttributeReleasePoliciesComponent implements OnInit {
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
    if (ReturnAllAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = "all";
    } else if (DenyAllAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = "deny";
    } else if (ReturnMappedAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = "mapped";
    } else if (ReturnAllowedAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = "allowed";
    } else if (ScriptedRegisteredServiceAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = "script";
    } else if (GroovyScriptAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = "groovy";
    }
  }

  changeType() {
    switch(this.type) {
      case "all" :
        this.service.attributeReleasePolicy = new ReturnAllAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case "deny" :
        this.service.attributeReleasePolicy = new DenyAllAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case "mapped" :
        let mapped: ReturnMappedAttributeReleasePolicy = new ReturnMappedAttributeReleasePolicy(this.service.attributeReleasePolicy);
        mapped.allowedAttributes = new Map();
        this.formData.availableAttributes.forEach((item: any) => {
          mapped.allowedAttributes[item] = [item];
        });
        this.service.attributeReleasePolicy = mapped;
        break;
      case "script" :
        this.service.attributeReleasePolicy = new ScriptedRegisteredServiceAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case "groovy" :
        this.service.attributeReleasePolicy = new GroovyScriptAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
    }
  }

  isEmpty(data: any[]): boolean {
    return !data || data.length == 0;
  }

}
