import {Component, Inject, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";
import {MD_DIALOG_DATA} from '@angular/material';
import {Messages} from "../messages";

@Component({
  selector: 'app-delete',
  templateUrl: './delete.component.html',
  styleUrls: ['./delete.component.css']
})
export class DeleteComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<DeleteComponent>,
              @Inject(MD_DIALOG_DATA) public data: any,
              public messages: Messages) { }

  ngOnInit() {
  }

}
