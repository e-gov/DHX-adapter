package ee.bpw.dhx.server.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxSendDocumentResult;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.model.OutgoingDhxPackage;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.AsyncDhxPackageService;
import ee.bpw.dhx.ws.service.DhxPackageProviderService;
import ee.bpw.dhx.ws.service.DhxPackageService;
import ee.bpw.dhx.ws.service.impl.DhxGateway;
import eu.x_road.dhx.producer.RepresentationListResponse;

@Service
public class DhxExampleService {

	@Autowired
	AddressService addressService;

	@Autowired
	DhxPackageProviderService dhxPackageProviderService;

	@Autowired
	DhxPackageService dhxPackageService;
	
	@Autowired
	AsyncDhxPackageService asyncDhxPackageService;
	
	@Autowired
	DhxGateway dhxGateway;

	public void sendExample() throws DhxException{
		//olemas palju overloaded meetodeid erinevate sisendite jaoks. vt. DhxPackageProviderService
		//saatjana pannakse konfist xtee liige
		OutgoingDhxPackage dhxPackage = dhxPackageProviderService
				.getOutgoingPackage(new File("example.txt"), "consignmentId",
						"recipientMemberCode", "recipientSubsystem");
		DhxSendDocumentResult result = dhxPackageService.sendPackage(dhxPackage);
		
	}
	
	public void renewAddressList () throws DhxException{
		addressService.renewAddressList();
	}
	
	public void sendAsyncExample() throws DhxException{
		//olemas palju overloaded meetodeid erinevate sisendite jaoks. vt. DhxPackageProviderService
		OutgoingDhxPackage dhxPackage = dhxPackageProviderService
				.getOutgoingPackage(new File("example.txt"), "consignmentId",
						"recipientMemberCode", "recipientSubsystem");
		//tulemus kohe ei anta. Siis kui satmine tehtud kutsutakse DhxImplementationSpecificService.saveSendResult
		asyncDhxPackageService.sendPackage(dhxPackage);
		
	}
	
	public void getRepresentees () throws DhxException{
		//otsing aadressiraamatust
		InternalXroadMember xroadMember = addressService.getClientForMemberCode("memberCode", "subsystem");
		RepresentationListResponse response = dhxGateway.getRepresentationList(xroadMember);
	}
}
