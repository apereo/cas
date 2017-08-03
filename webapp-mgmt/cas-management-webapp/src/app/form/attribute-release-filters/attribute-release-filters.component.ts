import {Component, OnInit, Input} from '@angular/core';
import {FormData} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {isEmpty} from "rxjs/operator/isEmpty";
import {RegisteredServiceRegexAttributeFilter} from "../../../domain/attribute-release";

@Component({
  selector: 'app-attribute-release-filters',
  templateUrl: './attribute-release-filters.component.html',
  styleUrls: ['./attribute-release-filters.component.css']
})

export class AttributeReleaseFiltersComponent implements OnInit {
  @Input()
  service: AbstractRegisteredService;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  attributeFilter: RegisteredServiceRegexAttributeFilter;

  constructor(public messages: Messages) { }

  ngOnInit() {
    if (!this.service.attributeReleasePolicy.attributeFilter ||
        Object.keys(this.service.attributeReleasePolicy.attributeFilter).length == 0) {
        let filter: RegisteredServiceRegexAttributeFilter = new RegisteredServiceRegexAttributeFilter();
        filter.pattern = "";
        this.service.attributeReleasePolicy.attributeFilter = filter;
    }
    this.attributeFilter = this.service.attributeReleasePolicy.attributeFilter as RegisteredServiceRegexAttributeFilter;
  }

  isEmpty(data: any[]) {
    return data != null && data.length == 0;
  }

}
