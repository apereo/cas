import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSnackBar} from '@angular/material';
import {DomainService} from './domain.service';
import {Messages} from 'app/messages';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {Database, Datasource} from '../database';

@Component({
  selector: 'app-domains',
  templateUrl: './domains.component.html',
  styleUrls: ['./domains.component.css']
})
export class DomainsComponent implements OnInit {
  displayedColumns = ['actions', 'name'];
  domainDatabase: Database<String> = new Database<String>();
  dataSource: Datasource<String> | null;
  selectedItem: String;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(public messages: Messages,
              private router: Router,
              private domainService: DomainService,
              public snackBar: MatSnackBar,
              private location: Location) { }

  ngOnInit() {
    this.dataSource = new Datasource(this.domainDatabase, this.paginator, this.filterFn);
    this.domainService.getDomains()
      .then(resp => this.domainDatabase.load(resp))
      .catch(e => {console.log(e); this.snackBar.open('Failed to load domains', 'Dismiss'); });
  }

  filterFn(item: String, filter: String): boolean {
    const searchStr = item.toLowerCase();
    return searchStr.indexOf(filter.toLowerCase()) !== -1;
  }

  doFilter(val: string) {
    if (!this.dataSource) { return; }
    this.dataSource.filter = val;
  }

  view(domain: String) {
    this.router.navigate(['services', domain]);
  }
}
