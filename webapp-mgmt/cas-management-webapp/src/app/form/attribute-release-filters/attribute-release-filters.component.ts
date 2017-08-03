import {Component, OnInit, Input} from '@angular/core';
import {FormData} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {RegisteredServiceRegexAttributeFilter} from "../../../domain/attribute-release";
import {Data} from "../data";
import {Util} from "../../util/util";

@Component({
  selector: 'app-attribute-release-filters',
  templateUrl: './attribute-release-filters.component.html',
  styleUrls: ['./attribute-release-filters.component.css']
})

export class AttributeReleaseFiltersComponent implements OnInit {

  attributeFilter: RegisteredServiceRegexAttributeFilter;

  constructor(public messages: Messages,
              private data: Data) {
    this.attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter
  }

  ngOnInit() {

  }

  updateFilter(pattern: String) {
    if (pattern && pattern != '') {
      this.attributeFilter = new RegisteredServiceRegexAttributeFilter();
      this.attributeFilter.pattern = pattern;
      this.data.service.attributeReleasePolicy.attributeFilter = this.attributeFilter;
    } else {
      this.attributeFilter = null;
      this.data.service.attributeReleasePolicy.attributeFilter = null;
    }
  }

  getPattern(): String {
    if (this.attributeFilter) {
      return this.attributeFilter.pattern;
    }
    return '';
  }
}
