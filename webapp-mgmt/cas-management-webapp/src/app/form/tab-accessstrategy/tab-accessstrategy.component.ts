import { Component, OnInit } from '@angular/core';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-accessstratefy',
  templateUrl: './tab-accessstrategy.component.html'
})
export class TabAccessstrategyComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
