import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {FormData,Data} from "../../../domain/form";

@Component({
  selector: 'app-uidattrs',
  templateUrl: './uidattrs.component.html'
})
export class UidattrsComponent implements OnInit {

  @Input()
  serviceData: Data;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
