import {Component, Input, OnInit} from '@angular/core';
import {ServiceData, FormData } from "../../../domain/service-edit-bean";
import {Messages} from "../../messages";

@Component({
  selector: 'app-wsfedattrrelpolicies',
  templateUrl: './wsfedattrrelpolicies.component.html',
  styleUrls: ['./wsfedattrrelpolicies.component.css']
})
export class WsfedattrrelpoliciesComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {}

  isEmpty(data: any[]) {
    return data != null && data.length == 0;
  }

}
