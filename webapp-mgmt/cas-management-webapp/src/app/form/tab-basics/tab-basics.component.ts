import { Component, OnInit } from '@angular/core';
import 'rxjs/add/operator/switchMap';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-basics',
  templateUrl: './tab-basics.component.html'
})
export class TabBasicsComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
