/**
 * Created by tsschmi on 2/28/17.
 */
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms'
import 'hammerjs';
import {
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDatepickerModule,
  MatDialogModule,
  MatExpansionModule,
  MatIconModule,
  MatInputModule,
  MatNativeDateModule,
  MatRadioModule,
  MatRippleModule,
  MatSelectModule,
  MatTableModule,
  MatTabsModule,
  MatTooltipModule,
  MatListModule,
  MatMenuModule,
  MatChipsModule,
  MatAutocompleteModule,
  MatSnackBarModule,
  MatPaginatorModule
} from '@angular/material';
import {CdkTableModule} from '@angular/cdk/table';
import {ControlsComponent} from '../controls/controls.component';
import {ControlsService} from '../controls/controls.service';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MatTabsModule,
    MatCheckboxModule,
    MatInputModule,
    MatIconModule,
    MatTooltipModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule,
    MatTableModule,
    CdkTableModule,
    MatButtonModule,
    MatRadioModule,
    MatRippleModule,
    MatCardModule,
    MatExpansionModule,
    MatListModule,
    MatMenuModule,
    MatChipsModule,
    MatDialogModule,
    MatAutocompleteModule,
    MatSnackBarModule,
    MatPaginatorModule
  ],
  declarations: [
    ControlsComponent
  ],
  providers: [
    ControlsService
  ],
  exports: [
    MatTabsModule,
    MatCheckboxModule,
    MatInputModule,
    MatIconModule,
    MatTooltipModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule,
    MatTableModule,
    CdkTableModule,
    MatButtonModule,
    MatRadioModule,
    MatRippleModule,
    MatCardModule,
    MatExpansionModule,
    MatListModule,
    MatMenuModule,
    MatChipsModule,
    MatDialogModule,
    MatAutocompleteModule,
    MatSnackBarModule,
    MatPaginatorModule,
    ControlsComponent
  ]
})
export class SharedModule {}
