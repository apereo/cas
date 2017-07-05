import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SamlservicespaneComponent } from './samlservicespane.component';

describe('SamlservicespaneComponent', () => {
  let component: SamlservicespaneComponent;
  let fixture: ComponentFixture<SamlservicespaneComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SamlservicespaneComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SamlservicespaneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
