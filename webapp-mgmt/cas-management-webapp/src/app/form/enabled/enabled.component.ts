import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-enabled',
  templateUrl: './enabled.component.html',
})
export class EnabledComponent implements OnInit {

  @Input()
  serviceData: Data;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
