import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Messages} from '../messages';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {HeaderService} from './header.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  @ViewChild('search') search: ElementRef;

  type: String;

  constructor(public messages: Messages,
              public router: Router,
              public location: Location,
              private service: HeaderService) { }

  ngOnInit() {
    this.service.getMangerType().then(resp => this.type = resp);
  }

  doSearch(val: string) {
    this.router.navigate( ['search', val]);
  }

  logout() {
    window.location.href = 'logout.html';
  }

}
