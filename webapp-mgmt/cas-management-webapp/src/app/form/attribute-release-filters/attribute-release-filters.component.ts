import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Messages} from '../../messages';
import {
    RegisteredServiceAttributeFilter, RegisteredServiceChainingAttributeFilter,
    RegisteredServiceMappedRegexAttributeFilter,
    RegisteredServiceRegexAttributeFilter, RegisteredServiceReverseMappedRegexAttributeFilter,
    RegisteredServiceScriptedAttributeFilter
} from '../../../domain/attribute-filter';
import {Data} from '../data';

@Component({
  selector: 'app-attribute-release-filters',
  templateUrl: './attribute-release-filters.component.html',
  styleUrls: ['./attribute-release-filters.component.css']
})

export class AttributeReleaseFiltersComponent implements OnInit {

  selectedFilter: RegisteredServiceAttributeFilter;

  @ViewChild('accordian')
  accordian: ElementRef;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
  }

  updateFilter(pattern: String) {
    let attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter as RegisteredServiceRegexAttributeFilter;
    if (pattern && pattern !== '') {
      attributeFilter = new RegisteredServiceRegexAttributeFilter();
      attributeFilter.pattern = pattern;
      this.data.service.attributeReleasePolicy.attributeFilter = attributeFilter;
    } else {
      attributeFilter = null;
      this.data.service.attributeReleasePolicy.attributeFilter = null;
    }
  }

  getPattern(): String {
    const attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter as RegisteredServiceRegexAttributeFilter;
    if (attributeFilter) {
      return attributeFilter.pattern;
    }
    return '';
  }

  filters(): RegisteredServiceAttributeFilter[] {
    const attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter;
    if (RegisteredServiceChainingAttributeFilter.instanceof(attributeFilter)) {
      return (attributeFilter as RegisteredServiceChainingAttributeFilter).filters;
    } else if (attributeFilter) {
      return [attributeFilter];
    } else {
      return [];
    }
  }

  addRegEx() {
    this.addFilter(new RegisteredServiceRegexAttributeFilter())
  }

  addMappedRegex() {
    this.addFilter(new RegisteredServiceMappedRegexAttributeFilter());
  }

  addReverseMapped() {
    this.addFilter(new RegisteredServiceReverseMappedRegexAttributeFilter());
  }

  addScript() {
    this.addFilter(new RegisteredServiceScriptedAttributeFilter());
  }

  addFilter(filter: any) {
      const attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter;
      if (RegisteredServiceChainingAttributeFilter.instanceof(attributeFilter)) {
          (attributeFilter as RegisteredServiceChainingAttributeFilter).filters.push(filter);
      } else if (attributeFilter) {
          const chaining = new RegisteredServiceChainingAttributeFilter();
          chaining.filters.push(attributeFilter);
          chaining.filters.push(filter);
          this.data.service.attributeReleasePolicy.attributeFilter = chaining;
      } else {
          this.data.service.attributeReleasePolicy.attributeFilter = filter;
      }
  }

  removeFilter() {
    if (this.isChaining(this.data.service.attributeReleasePolicy.attributeFilter)) {
      const filters = (this.data.service.attributeReleasePolicy.attributeFilter as RegisteredServiceChainingAttributeFilter).filters;
      filters.splice(filters.indexOf(this.selectedFilter), 1);
      if (filters.length > 1) {
        return;
      } else if (filters.length === 1) {
        this.data.service.attributeReleasePolicy.attributeFilter = filters[0];
        return;
      }
    }
    this.data.service.attributeReleasePolicy.attributeFilter = null;
  }

  isRegEx(filter: any): boolean {
    return RegisteredServiceRegexAttributeFilter.instanceOf(filter);
  }

  isChaining(filter: any): boolean {
    return RegisteredServiceChainingAttributeFilter.instanceof(filter);
  }

  isMappedRegEx(filter: any): boolean {
    return RegisteredServiceMappedRegexAttributeFilter.instanceof(filter);
  }

  isReverseMapped(filter: any): boolean {
    return RegisteredServiceReverseMappedRegexAttributeFilter.instanceof(filter);
  }

  isScripted(filter: any): boolean {
    return RegisteredServiceScriptedAttributeFilter.instanceof(filter);
  }

  getAttributes(filter: RegisteredServiceMappedRegexAttributeFilter) {
    return Object.keys(filter.patterns);
  }

  moveUp() {
    const attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter as RegisteredServiceChainingAttributeFilter;
    const i = attributeFilter.filters.indexOf(this.selectedFilter);
    if (i === 0) {
      return;
    }
    attributeFilter.filters[i] = attributeFilter.filters[i - 1];
    attributeFilter.filters[i - 1] = this.selectedFilter;
  }

    moveDown() {
        const attributeFilter = this.data.service.attributeReleasePolicy.attributeFilter as RegisteredServiceChainingAttributeFilter;
        const i = attributeFilter.filters.indexOf(this.selectedFilter);
        if (i === attributeFilter.filters.length - 1) {
            return;
        }
        attributeFilter.filters[i] = attributeFilter.filters[i + 1];
        attributeFilter.filters[i + 1] = this.selectedFilter;
    }

}


