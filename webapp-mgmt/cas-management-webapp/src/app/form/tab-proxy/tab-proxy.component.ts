import { Component, OnInit } from '@angular/core';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-proxy',
  templateUrl: './tab-proxy.component.html'
})
export class TabProxyComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
