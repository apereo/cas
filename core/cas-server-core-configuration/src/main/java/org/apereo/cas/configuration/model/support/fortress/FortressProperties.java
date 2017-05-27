package org.apereo.cas.configuration.model.support.fortress;

public class FortressProperties {

  private String rbacContextId = "HOME";
  private String name = "fortressHandler";
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getRbacContextId() {
    return rbacContextId;
  }

  public void setRbacContextId(String rbacContextId) {
    this.rbacContextId = rbacContextId;
  }

}
