import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Messages} from './messages';
import {HeaderService} from './header/header.service';

@Component({
  selector: 'app-init',
  template: ''
})

export class InitComponent implements OnInit {

  constructor(private router: Router,
              public messages: Messages,
              public service: HeaderService) {
  }

  ngOnInit() {
    this.service.getMangerType().then(resp => {
      if (resp === 'DOMAIN') {
        this.router.navigate(['/domains']);
      } else {
        this.router.navigate(['services', 'default']);
      }
    });
  }
}
