package ee.ria.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.AsyncDhxSendDocumentResult;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;

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
public class ExampleDhxImplementationSpecificService
    implements
      DhxImplementationSpecificService {

  private List<IncomingDhxPackage> documents = new ArrayList<IncomingDhxPackage>();

  private List<InternalXroadMember> members;

  @Override
  @Deprecated
  @Loggable
  public boolean isDuplicatePackage(InternalXroadMember from,
      String consignmentId) throws DhxException {
    log.debug("Checking for duplicates. from memberCode: {}",
        from.toString() + " from consignmentId:" + consignmentId);
    if (documents != null && documents.size() > 0) {
      for (IncomingDhxPackage document : documents) {
        if (document.getExternalConsignmentId() != null
            && document.getExternalConsignmentId().equals(
                consignmentId)
            && (document.getClient().toString()
                .equals(from.toString()) || document
                    .getClient().getRepresentee().getRepresenteeCode()
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
  public List<InternalXroadMember> getAdresseeList() throws DhxException {
    return members;
  }

  @Override
  @Deprecated
  public void saveAddresseeList(List<InternalXroadMember> members) throws DhxException {
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
