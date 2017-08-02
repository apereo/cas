import {Component, OnInit, Input} from '@angular/core';
import {FormData} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-attribute-release-checks',
  templateUrl: './attribute-release-checks.component.html',
  styleUrls: ['./attribute-release-checks.component.css']
})
export class AttributeReleaseChecksComponent implements OnInit {
  @Input()
  service: AbstractRegisteredService;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

  isEmpty(data: any[]) {
    return data != null && data.length == 0;
  }

}
