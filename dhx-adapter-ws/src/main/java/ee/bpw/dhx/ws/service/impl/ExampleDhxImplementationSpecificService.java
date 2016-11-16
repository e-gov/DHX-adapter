package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.AsyncDhxSendDocumentResult;
import ee.bpw.dhx.model.DhxRepresentee;
import ee.bpw.dhx.model.DhxSendDocumentResult;
import ee.bpw.dhx.model.IncomingDhxPackage;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ws.context.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class is an example implementation of DhxImplementationSpecificService interface. All data that
 * need to be stored is stored in memory.
 * 
 * @deprecated - its just an example implementation. real implementation should be done!
 * @author Aleksei Kokarev
 *
 */
@Slf4j
@Deprecated
public class ExampleDhxImplementationSpecificService implements
    DhxImplementationSpecificService {

  private List<IncomingDhxPackage> documents = new ArrayList<IncomingDhxPackage>();

  private List<InternalXroadMember> members;

  @Override
  @Deprecated
  @Loggable
  public boolean isDuplicatePackage(InternalXroadMember from,
      String consignmentId) {
    log.debug("Checking for duplicates. from memberCode: {}",
        from.toString() + " from consignmentId:" + consignmentId);
    if (documents != null && documents.size() > 0) {
      for (IncomingDhxPackage document : documents) {
        if (document.getExternalConsignmentId() != null
            && document.getExternalConsignmentId().equals(
                consignmentId)
            && (document.getClient().toString()
                .equals(from.toString()) || document
                .getClient().getRepresentee().getMemberCode()
                .equals(from.toString()))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  @Deprecated
  @Loggable
  public String receiveDocument(IncomingDhxPackage document,
      MessageContext context) throws DhxException {
    log.debug(
        "String receiveDocument(DhxDocument document) externalConsignmentId: {}",
        document.getExternalConsignmentId());
    String receiptId = UUID.randomUUID().toString();
    documents.add(document);
    return receiptId;
  }

  @Override
  @Deprecated
  @Loggable
  public List<DhxRepresentee> getRepresentationList() {
    return new ArrayList<DhxRepresentee>();
  }

  @Override
  @Deprecated
  public List<InternalXroadMember> getAdresseeList() {
    return members;
  }

  @Override
  @Deprecated
  public void saveAddresseeList(List<InternalXroadMember> members) {
    this.members = members;
  }

  @Override
  @Deprecated
  public void saveSendResult(DhxSendDocumentResult finalResult,
      List<AsyncDhxSendDocumentResult> retryResults) {
    log.info("Got results for document sending. Recipient: "
        + finalResult.getSentPackage().getService()
        + " ConsignmentId:"
        + finalResult.getSentPackage().getInternalConsignmentId()
        + " Result: receiptId:"
        + finalResult.getResponse().getReceiptId()
        + " fault:"
        + (finalResult.getResponse().getFault() == null ? "" : "code:"
            + finalResult.getResponse().getFault().getFaultCode()
            + " String:"
            + finalResult.getResponse().getFault().getFaultString()));
  }

}
