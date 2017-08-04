import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  RefuseRegisteredServiceProxyPolicy,
  RegexMatchingRegisteredServiceProxyPolicy
} from "../../../domain/proxy-policy,ts";
import {Data} from "../data";

enum Type {
  REGEX,
  REFUSE
}

@Component({
  selector: 'app-proxy',
  templateUrl: './proxy.component.html'
})
export class ProxyComponent implements OnInit {

  service: AbstractRegisteredService;
  type: Type;
  TYPE = Type;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
  }

  ngOnInit() {
    if (RefuseRegisteredServiceProxyPolicy.instanceOf(this.service.proxyPolicy)) {
      this.type = Type.REFUSE;
    } else if (RegexMatchingRegisteredServiceProxyPolicy.instanceOf(this.service.proxyPolicy)) {
      this.type = Type.REGEX;
    }
  }

  changeType() {
    switch(+this.type) {
      case Type.REFUSE :
        this.service.proxyPolicy = new RefuseRegisteredServiceProxyPolicy();
        break;
      case Type.REGEX :
        this.service.proxyPolicy = new RegexMatchingRegisteredServiceProxyPolicy();
        break;
    }
  }

}
