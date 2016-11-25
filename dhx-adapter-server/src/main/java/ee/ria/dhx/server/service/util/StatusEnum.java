package ee.ria.dhx.server.service.util;

import lombok.Getter;

public enum StatusEnum {
  
  IN_PROCESS("edastamisel"), RECEIVED("vastu võetud"), FAILED("edastamine ebaõnnestus"); 
  
  @Getter
  String classificatorName;
  
  private StatusEnum (String classificatorName) {
    this.classificatorName = classificatorName;
  }

}
