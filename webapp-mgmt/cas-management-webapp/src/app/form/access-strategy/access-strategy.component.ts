import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data,FormData} from "../../../domain/form";

@Component({
  selector: 'app-access-strategy',
  templateUrl: './access-strategy.component.html',
})
export class AccessStrategyComponent implements OnInit {

  @Input()
  formData: FormData;

  @Input()
  serviceData: Data;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
