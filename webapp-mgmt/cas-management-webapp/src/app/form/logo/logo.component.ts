import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-logo',
  templateUrl: './logo.component.html'
})
export class LogoComponent implements OnInit {

  @Input()
  serviceData: Data;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
