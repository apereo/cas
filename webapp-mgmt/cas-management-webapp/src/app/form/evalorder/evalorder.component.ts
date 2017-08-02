import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-evalorder',
  templateUrl: './evalorder.component.html'
})
export class EvalorderComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
