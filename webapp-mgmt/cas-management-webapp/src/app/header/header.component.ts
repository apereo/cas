import { Component, OnInit } from '@angular/core';
import {Messages} from "../messages";
import {Router} from "@angular/router";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  isAdmin: boolean = false;

  constructor(public messages: Messages,
              public router: Router) { }

  ngOnInit() {
  }

  search(query: String) {
    this.router.navigate(['search']);
  }

}
