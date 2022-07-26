package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.AsyncDhxSendDocumentResult;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Fault;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxPackageService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class AsyncDhxPackageServiceImplTest {

  @Mock
  DhxImplementationSpecificService specificService;

  @Mock
  DhxPackageService dhxPackageService;

  @Mock
  DhxConfig config;

  AsyncDhxPackageServiceImpl asyncDhxPackageServiceImpl;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    asyncDhxPackageServiceImpl = new AsyncDhxPackageServiceImpl();
    asyncDhxPackageServiceImpl.setConfig(config);
    asyncDhxPackageServiceImpl.setDhxImplementationSpecificService(specificService);
    asyncDhxPackageServiceImpl.setDhxPackageService(dhxPackageService);
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(1);
    list.add(1);
    asyncDhxPackageServiceImpl.setResendTimeouts(list);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sendPackage() throws DhxException {
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    SendDocumentResponse resp = new SendDocumentResponse();
    resp.setReceiptId("receipt id");
    DhxSendDocumentResult result = new DhxSendDocumentResult(pckg, resp);
    when(dhxPackageService.sendPackage(pckg)).thenReturn(result);
    asyncDhxPackageServiceImpl.sendPackage(pckg);
    verify(dhxPackageService, times(1)).sendPackage(pckg);
    verify(specificService).saveSendResult(Mockito.eq(result), any(List.class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void sendPackageFailed() throws DhxException {
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    SendDocumentResponse resp = new SendDocumentResponse();
    resp.setReceiptId("receipt id");
    Fault fault = new Fault();
    fault.setFaultCode("ERROR");
    fault.setFaultString("fault");
    resp.setFault(fault);
    DhxSendDocumentResult result = new DhxSendDocumentResult(pckg, resp);
    when(dhxPackageService.sendPackage(pckg)).thenReturn(result);
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    asyncDhxPackageServiceImpl.sendPackage(pckg);
    verify(dhxPackageService, times(4)).sendPackage(pckg);
    verify(specificService).saveSendResult(Mockito.eq(result), argument.capture());
    List<AsyncDhxSendDocumentResult> results = argument.getValue();
    assertEquals(4, results.size());
    for (AsyncDhxSendDocumentResult asyncResult : results) {
      assertEquals(asyncResult.getResult(), result);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sendMultiplePackages() throws DhxException {
    List<OutgoingDhxPackage> pckgs = new ArrayList<OutgoingDhxPackage>();
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    pckgs.add(pckg);
    pckgs.add(pckg);
    SendDocumentResponse resp = new SendDocumentResponse();
    resp.setReceiptId("receipt id");
    DhxSendDocumentResult result = new DhxSendDocumentResult(pckg, resp);
    when(dhxPackageService.sendPackage(pckg)).thenReturn(result);
    asyncDhxPackageServiceImpl.sendMultiplePackages(pckgs);
    verify(dhxPackageService, times(2)).sendPackage(pckg);
    verify(specificService, times(2)).saveSendResult(Mockito.eq(result), any(List.class));
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void sendMultiplePackagesFailed() throws DhxException {
    List<OutgoingDhxPackage> pckgs = new ArrayList<OutgoingDhxPackage>();
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    pckgs.add(pckg);
    pckgs.add(pckg);
    SendDocumentResponse resp = new SendDocumentResponse();
    resp.setReceiptId("receipt id");
    Fault fault = new Fault();
    fault.setFaultCode("ERROR");
    fault.setFaultString("fault");
    resp.setFault(fault);
    DhxSendDocumentResult result = new DhxSendDocumentResult(pckg, resp);
    when(dhxPackageService.sendPackage(pckg)).thenReturn(result);
    asyncDhxPackageServiceImpl.sendMultiplePackages(pckgs);
    verify(dhxPackageService, times(8)).sendPackage(pckg);
    verify(specificService, times(2)).saveSendResult(Mockito.eq(result), any(List.class));
  }


}
