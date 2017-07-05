import { Component, OnInit } from '@angular/core';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-advanced',
  templateUrl: './tab-advanced.component.html'
})
export class TabAdvancedComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
