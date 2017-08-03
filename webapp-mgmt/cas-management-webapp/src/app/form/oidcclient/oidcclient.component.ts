import {Component, Input, OnInit} from '@angular/core';
import {OidcRegisteredService} from "../../../domain/oauth-service";
import {Messages} from "../../messages";
import {Data} from "../data";

@Component({
  selector: 'app-oidcclient',
  templateUrl: './oidcclient.component.html',
  styleUrls: ['./oidcclient.component.css']
})
export class OidcclientComponent implements OnInit {

  service: OidcRegisteredService;
  selectOptions;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service as OidcRegisteredService;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if(!this.service.scopes) {
      this.service.scopes = [];
    }
  }

}
