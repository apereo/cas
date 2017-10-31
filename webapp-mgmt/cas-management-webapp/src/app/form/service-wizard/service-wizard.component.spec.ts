import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceWizardComponent } from './service-wizard.component';

describe('ServiceWizardComponent', () => {
  let component: ServiceWizardComponent;
  let fixture: ComponentFixture<ServiceWizardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceWizardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
