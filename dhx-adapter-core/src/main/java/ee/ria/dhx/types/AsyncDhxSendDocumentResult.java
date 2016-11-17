package ee.ria.dhx.types;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AsyncDhxSendDocumentResult {

  public AsyncDhxSendDocumentResult(DhxSendDocumentResult result) {
    this.result = result;
    this.tryDate = new Date();
  }

  DhxSendDocumentResult result;
  Date tryDate;

}
