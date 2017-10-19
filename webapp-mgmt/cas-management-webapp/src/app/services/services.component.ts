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
import {ControlsService} from "../controls/controls.service";
import {DataSource} from "@angular/cdk/table";

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit,AfterViewInit {
  deleteItem: ServiceItem;
  domain: String;
  selectedItem: ServiceItem;
  revertItem: ServiceItem;
  serviceDatabase = new ServiceDatabase();
  dataSource: ServiceDataSource | null;
  displayedColumns = ['actions','name','serviceId','description'];

  @ViewChild("paginator")
  paginator: MatPaginator;

  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private service: ServiceViewService,
              private location: Location,
              public dialog: MatDialog,
              public snackBar: MatSnackBar,
              public controlsService: ControlsService) {
  }

  ngOnInit() {
    this.dataSource = new ServiceDataSource(this.serviceDatabase,this.paginator);
    this.route.data
      .subscribe((data: { resp: ServiceItem[]}) => {
        if (!data.resp) {
          this.snackBar.open(this.messages.management_services_status_listfail,'dismiss',{
            duration: 5000
          });
        }
        this.serviceDatabase.load(data.resp);
      });
    this.route.params.subscribe((params) => this.domain = params['domain']);
  }

  ngAfterViewInit() {

  }

  serviceEdit(item?: ServiceItem) {
    if (item) {
      this.selectedItem = item;
    }
    this.router.navigate(['/form',this.selectedItem.assignedId]);
  }

  serviceDuplicate() {
    this.router.navigate(['/duplicate',this.selectedItem.assignedId]);
  }

  openModalDelete() {
    let dialogRef = this.dialog.open(DeleteComponent,{
      data: this.selectedItem,
      width: '500px',
      position: {top: '100px'}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.delete();
      }
    });
    this.deleteItem = this.selectedItem;
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
      .then(resp => this.serviceDatabase.load(resp))
      .catch((e: any) => this.snackBar.open(this.messages.management_services_status_listfail,'Dismiss', {
        duration: 5000
      }));
  }

  moveUp(a: ServiceItem) {
    let index: number = this.serviceDatabase.data.indexOf(a);
    if(index > 0) {
      let b: ServiceItem = this.serviceDatabase.data[index - 1];
      a.evalOrder = index-1;
      b.evalOrder = index;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  moveDown(a: ServiceItem) {
    let index: number = this.serviceDatabase.data.indexOf(a);
    if(index < this.serviceDatabase.data.length -1) {
      let b: ServiceItem = this.serviceDatabase.data[index + 1];
      a.evalOrder = index+1;
      b.evalOrder = index;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  showMoveUp(): boolean {
    if (!this.selectedItem) {
      return false;
    }
    let index = this.serviceDatabase.data.indexOf(this.selectedItem);
    return index > 0;
  }

  showMoveDown(): boolean {
    if (!this.selectedItem) {
      return false;
    }
    let index = this.serviceDatabase.data.indexOf(this.selectedItem);
    return index < this.serviceDatabase.data.length - 1;
  }

}

export class ServiceDatabase {
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

export class ServiceDataSource extends DataSource<any> {

  constructor(private _serviceDatabase: ServiceDatabase, private _paginator: MatPaginator) {
    super();
  }

  connect(): Observable<ServiceItem[]> {
    const displayDataChanges = [
      this._serviceDatabase.dataChange,
      this._paginator.page,
    ];

    return Observable.merge(...displayDataChanges).map(() => {
      const data = this._serviceDatabase.data.slice();
      const startIndex = this._paginator.pageIndex * this._paginator.pageSize;
      return data.splice(startIndex, this._paginator.pageSize);
    });
  }

  disconnect() {}
}
