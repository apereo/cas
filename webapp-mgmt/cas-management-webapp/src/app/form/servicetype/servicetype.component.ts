import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-servicetype',
  templateUrl: './servicetype.component.html'
})
export class ServicetypeComponent implements OnInit {

  @Input()
  serviceData: Data;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
