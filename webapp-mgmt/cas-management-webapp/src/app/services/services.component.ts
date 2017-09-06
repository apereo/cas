import { Component, OnInit, ViewChild } from '@angular/core';
import {ServiceViewBean} from "../../domain/service-view-bean";
import {Messages} from "../messages";
import {ActivatedRoute, Router} from "@angular/router";
import {ServiceViewService} from "./service.service";
import {Location} from "@angular/common";
import {MdDialog, MdSnackBar} from "@angular/material";
import {DeleteComponent} from "../delete/delete.component";

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit {

  dataTable: ServiceViewBean[];
  detailRow: String;
  deleteItem: ServiceViewBean;
  domain: String;
  selectedItem: ServiceViewBean;

  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private service: ServiceViewService,
              private location: Location,
              public dialog: MdDialog,
              public snackBar: MdSnackBar) {
    this.dataTable = [];
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { resp: ServiceViewBean[]}) => {
        this.dataTable = data.resp;
        if (!this.dataTable) {
          this.snackBar.open(this.messages.management_services_status_listfail,'dismiss',{
            duration: 5000
          });
        }
      });
    this.route.params.subscribe((params) => this.domain = params['domain']);
  }

  serviceEdit(selectedItem: String) {
    this.router.navigate(['/form',selectedItem]);
  }

  serviceDuplicate(selectedItem: String) {
    this.router.navigate(['/form',selectedItem, {duplicate: true}]);
  }

  toggleDetail(id: String) {
    if (this.detailRow != id) {
      this.detailRow = id;
    } else {
      this.detailRow = null;
    }
  }

  openModalDelete(selectedItem: ServiceViewBean) {
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
      .catch((e: any) => this.snackBar.open(this.messages.management_services_status_notdeleted, 'Dismiss', {
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
    setTimeout(() => {
      this.getServices();
    },500);
  }

  getServices() {
    this.service.getServices(this.domain)
      .then(resp => this.dataTable = resp)
      .catch((e: any) => this.snackBar.open(this.messages.management_services_status_listfail,'Dismiss', {
        duration: 5000
      }));
  }

  goBack() {
    this.location.back();
  }

  moveUp(a: ServiceViewBean) {
    let index: number = this.dataTable.indexOf(a);
    if(index > 0) {
      let b: ServiceViewBean = this.dataTable[index-1];
      a.evalOrder = index-1;
      b.evalOrder = index;
      this.dataTable[index] = b;
      this.dataTable[index -1] = a;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  moveDown(a: ServiceViewBean) {
    let index: number = this.dataTable.indexOf(a);
    if(index < this.dataTable.length -1) {
      let b: ServiceViewBean = this.dataTable[index+1];
      a.evalOrder = index+1;
      b.evalOrder = index;
      this.dataTable[index] = b;
      this.dataTable[index+1] = a;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

}
