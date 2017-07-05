import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html'
})
export class LogoutComponent implements OnInit {

  @Input()
  serviceData: Data;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
