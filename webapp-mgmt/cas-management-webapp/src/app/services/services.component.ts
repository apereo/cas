import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {ServiceItem} from "../../domain/service-view-bean";
import {Messages} from "../messages";
import {ActivatedRoute, Router} from "@angular/router";
import {ServiceViewService} from "./service.service";
import {Location} from "@angular/common";
import {MatDialog, MatPaginator, MatSnackBar} from "@angular/material";
import {DeleteComponent} from "../delete/delete.component";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit,AfterViewInit {

  dataTable: ServiceItem[];
  deleteItem: ServiceItem;
  domain: String;
  selectedItem: ServiceItem;
  servicesDatabase = new ServicesDatabase();

  @ViewChild("paginator")
  paginator: MatPaginator;

  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private service: ServiceViewService,
              private location: Location,
              public dialog: MatDialog,
              public snackBar: MatSnackBar) {
    this.dataTable = [];
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { resp: ServiceItem[]}) => {
        if (!data.resp) {
          this.snackBar.open(this.messages.management_services_status_listfail,'dismiss',{
            duration: 5000
          });
        }
        this.servicesDatabase.load(data.resp);
      });
    this.route.params.subscribe((params) => this.domain = params['domain']);
  }

  ngAfterViewInit() {
    const displayDataChanges = [
      this.servicesDatabase.dataChange,
      this.paginator.page,
    ];

    Observable.merge(...displayDataChanges).map(() => {
      const data = this.servicesDatabase.data.slice();
      const startIndex = this.paginator.pageIndex * this.paginator.pageSize;
      return data.splice(startIndex, this.paginator.pageSize);
    }).subscribe((d) => setTimeout(() => this.dataTable = d,0));
  }

  serviceEdit(selectedItem: String) {
    this.router.navigate(['/form',selectedItem, {duplicate: false}]);
  }

  serviceDuplicate(selectedItem: String) {
    this.router.navigate(['/form',selectedItem, {duplicate: true}]);
  }

  openModalDelete(selectedItem: ServiceItem) {
    let dialogRef = this.dialog.open(DeleteComponent,{
      data: selectedItem,
      width: '500px',
      position: {top: '100px'}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.delete();
      }
    });
    this.deleteItem = selectedItem;
  };

  delete() {
    let myData = {id: this.deleteItem.assignedId};

    this.service.delete(Number.parseInt(this.deleteItem.assignedId as string))
      .then(resp => this.handleDelete(resp))
      .catch((e: any) => this.snackBar.open(e.message || e.text(), 'Dismiss', {
        duration: 5000
      }));
  };

  handleDelete(name: String) {
    this.snackBar.open(name+" "+this.messages.management_services_status_deleted,'Dismiss', {
      duration: 5000
    });
    this.refresh();
  }

  refresh() {
    this.getServices();
  }

  getServices() {
    this.service.getServices(this.domain)
      .then(resp => this.servicesDatabase.load(resp))
      .catch((e: any) => this.snackBar.open(this.messages.management_services_status_listfail,'Dismiss', {
        duration: 5000
      }));
  }

  goBack() {
    this.location.back();
  }

  moveUp(a: ServiceItem) {
    let index: number = this.servicesDatabase.data.indexOf(a);
    if(index > 0) {
      let b: ServiceItem = this.servicesDatabase.data[index - 1];
      a.evalOrder = index-1;
      b.evalOrder = index;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  moveDown(a: ServiceItem) {
    let index: number = this.servicesDatabase.data.indexOf(a);
    if(index < this.servicesDatabase.data.length -1) {
      let b: ServiceItem = this.servicesDatabase.data[index + 1];
      a.evalOrder = index+1;
      b.evalOrder = index;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

}

export class ServicesDatabase {
  dataChange: BehaviorSubject<ServiceItem[]> = new BehaviorSubject<ServiceItem[]>([]);
  get data(): ServiceItem[] { return this.dataChange.value; }

  constructor() {
  }

  load(services: ServiceItem[]) {
    this.dataChange.next([]);
    for(let service of services) {
      this.addService(service);
    }
  }

  addService(service: ServiceItem) {
    const copiedData = this.data.slice();
    copiedData.push(service);
    this.dataChange.next(copiedData);
  }
}
