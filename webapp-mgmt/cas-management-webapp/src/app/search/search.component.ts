import {Component, OnInit, ViewChild} from '@angular/core';
import {ServiceItem} from "../../domain/service-view-bean";
import {MatPaginator, MatSnackBar} from "@angular/material";
import {Messages} from "../messages";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {Location} from "@angular/common";
import {SearchService} from "./SearchService";
import {Database, Datasource} from "../database";

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {
  displayedColumns = ['name','serviceId','description'];
  serviceDatabase: Database<ServiceItem> = new Database<ServiceItem>();
  dataSource: Datasource<ServiceItem> | null;
  query: String;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(public messages: Messages,
              public router: Router,
              public route: ActivatedRoute,
              public location: Location,
              private service: SearchService,
              public snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new Datasource(this.serviceDatabase, this.paginator);
    this.route.paramMap
        .switchMap((params: ParamMap) => this.service.search(params.get('query')))
        .subscribe(resp => this.serviceDatabase.load(resp));
  }

  serviceEdit(id: number) {
    this.router.navigate(['form',id]);
  }

}

