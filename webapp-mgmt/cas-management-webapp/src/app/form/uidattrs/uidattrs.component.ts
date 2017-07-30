import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {FormData,ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-uidattrs',
  templateUrl: './uidattrs.component.html'
})
export class UidattrsComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
