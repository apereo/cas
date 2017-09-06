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


  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {

  }

  updateFilter(pattern: String) {
    let attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter;
    if (pattern && pattern != '') {
      attributeFilter = new RegisteredServiceRegexAttributeFilter();
      attributeFilter.pattern = pattern;
      this.data.service.attributeReleasePolicy.attributeFilter = attributeFilter;
    } else {
      attributeFilter = null;
      this.data.service.attributeReleasePolicy.attributeFilter = null;
    }
  }

  getPattern(): String {
    let attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter;
    if (attributeFilter) {
      return attributeFilter.pattern;
    }
    return '';
  }
}
