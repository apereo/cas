import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  RefuseRegisteredServiceProxyPolicy,
  RegexMatchingRegisteredServiceProxyPolicy
} from "../../../domain/proxy-policy,ts";
import {Data} from "../data";

@Component({
  selector: 'app-proxy',
  templateUrl: './proxy.component.html'
})
export class ProxyComponent implements OnInit {

  service: AbstractRegisteredService;
  type: String;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
  }

  ngOnInit() {
    if (RefuseRegisteredServiceProxyPolicy.instanceOf(this.service.proxyPolicy)) {
      this.type = "REFUSE";
    } else if (RegexMatchingRegisteredServiceProxyPolicy.instanceOf(this.service.proxyPolicy)) {
      this.type = "REGEX";
    }
  }

  changeType() {
    switch(this.type) {
      case "REFUSE" :
        this.service.proxyPolicy = new RefuseRegisteredServiceProxyPolicy();
        break;
      case "REGEX" :
        this.service.proxyPolicy = new RegexMatchingRegisteredServiceProxyPolicy();
        break;
    }
  }

}
