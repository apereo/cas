import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {FormData} from "../../../domain/service-view-bean";
import {RegisteredServiceRegexAttributeFilter} from "../../../domain/attribute-release";
import {Data} from "../data";

@Component({
  selector: 'app-attribute-release',
  templateUrl: './attribute-release.component.html',
})

export class AttributeReleaseComponent implements OnInit {

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

  }

}
