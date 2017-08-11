import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";

@Component({
  selector: 'app-serviceid',
  templateUrl: './serviceid.component.html'
})
export class ServiceidComponent implements OnInit {


  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
  }

}
