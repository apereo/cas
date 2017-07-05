import { Component, OnInit } from '@angular/core';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-servicetype',
  templateUrl: './tab-servicetype.component.html'
})
export class TabServicetypeComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
