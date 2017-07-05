import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-logouttypeeval',
  templateUrl: './logouttypeeval.component.html'
})
export class LogouttypeevalComponent implements OnInit {

  @Input()
  serviceData: Data;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
