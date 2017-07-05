import {Component, Input, OnInit} from '@angular/core';
import {Data, FormData } from "../../../domain/form";
import {Messages} from "../../messages";

@Component({
  selector: 'app-wsfedattrrelpolicies',
  templateUrl: './wsfedattrrelpolicies.component.html',
  styleUrls: ['./wsfedattrrelpolicies.component.css']
})
export class WsfedattrrelpoliciesComponent implements OnInit {

  @Input()
  serviceData: Data;

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
