package ee.ria.dhx.server.service;

import java.io.File;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.eu.x_road.dhx.producer.RepresentationListResponse;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;
import ee.ria.dhx.ws.service.DhxPackageService;
import ee.ria.dhx.ws.service.impl.DhxGateway;


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
