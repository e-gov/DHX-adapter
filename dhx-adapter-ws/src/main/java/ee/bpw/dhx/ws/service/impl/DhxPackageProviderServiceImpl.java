package ee.bpw.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.exception.DhxExceptionEnum;
import ee.bpw.dhx.model.CapsuleAdressee;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.model.OutgoingDhxPackage;
import ee.bpw.dhx.util.CapsuleVersionEnum;
import ee.bpw.dhx.util.FileUtil;
import ee.bpw.dhx.ws.config.CapsuleConfig;
import ee.bpw.dhx.ws.config.DhxConfig;
import ee.bpw.dhx.ws.config.SoapConfig;
import ee.bpw.dhx.ws.service.AddressService;
import ee.bpw.dhx.ws.service.DhxMarshallerService;
import ee.bpw.dhx.ws.service.DhxPackageProviderService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DhxPackageProviderServiceImpl implements DhxPackageProviderService {

  @Autowired
  SoapConfig soapConfig;

  @Autowired
  AddressService addressService;

  @Autowired
  CapsuleConfig capsuleConfig;

  @Autowired
  DhxConfig dhxConfig;

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, InternalXroadMember recipient)
      throws DhxException {
    return getOutgoingPackage(capsuleFile, consignmentId, recipient,
        soapConfig.getDefaultClient());
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, InternalXroadMember recipient,
      InternalXroadMember sender) throws DhxException {
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    return getOutgoingPackage(stream, consignmentId, recipient, sender);
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, InternalXroadMember recipient,
      InternalXroadMember sender) throws DhxException {
    dhxMarshallerService.checkFileSize(capsuleStream);
    InputStream schemaStream = null;
    OutgoingDhxPackage document = null;
    try {
      schemaStream = FileUtil
          .getFileAsStream(capsuleConfig
              .getXsdForVersion(capsuleConfig
                  .getCurrentCapsuleVersion()));
      document = getOutgoingPackage(capsuleStream, consignmentId,
          recipient, sender, schemaStream,
          dhxConfig.getProtocolVersion());
    } finally {
      FileUtil.safeCloseStream(schemaStream);
    }
    return document;
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, InternalXroadMember recipient,
      InternalXroadMember sender, InputStream schemaStream,
      String dhxProtocolVersion) throws DhxException {
    OutgoingDhxPackage document = new OutgoingDhxPackage(recipient, sender,
        capsuleStream, consignmentId, dhxProtocolVersion);
    try {
      if (dhxConfig.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        dhxMarshallerService.validate(document.getDocumentFile()
            .getInputStream(), schemaStream);
      } else {
        log.debug("Validating capsule is disabled");
      }
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.WS_ERROR,
          "Error occured while reading or writing capsule file.", ex);
    }
    return document;
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderMemberCode, String senderSubsystem,
      InputStream schemaStream, String dhxProtocolVersion)
      throws DhxException {
    InternalXroadMember recipient = addressService.getClientForMemberCode(
        recipientCode, recipientSystem);
    InternalXroadMember sender = addressService.getClientForMemberCode(
        senderMemberCode, senderSubsystem);
    return getOutgoingPackage(capsuleStream, consignmentId, recipient,
        sender, schemaStream, dhxConfig.getProtocolVersion());
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, InternalXroadMember recipient)
      throws DhxException {
    return getOutgoingPackage(capsuleStream, consignmentId, recipient,
        soapConfig.getDefaultClient());
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, String recipientCode, String recipientSystem)
      throws DhxException {
    return getOutgoingPackage(capsuleFile, consignmentId, recipientCode,
        recipientSystem, null);
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderSubsystem) throws DhxException {
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    return getOutgoingPackage(stream, consignmentId, recipientCode,
        recipientSystem, senderSubsystem);
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem)
      throws DhxException {
    return getOutgoingPackage(capsuleStream, consignmentId, recipientCode,
        recipientSystem, null);

  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderSubsystem) throws DhxException {
    InternalXroadMember adressee = addressService.getClientForMemberCode(
        recipientCode, recipientSystem);
    InternalXroadMember sender = soapConfig.getDefaultClient();
    if (senderSubsystem != null) {
      sender.setSubsystemCode(senderSubsystem);
    }
    return getOutgoingPackage(capsuleStream, consignmentId, adressee,
        sender);
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderMemberCode, String senderSubsystem)
      throws DhxException {
    InternalXroadMember adressee = addressService.getClientForMemberCode(
        recipientCode, recipientSystem);
    InternalXroadMember sender = addressService.getClientForMemberCode(
        senderMemberCode, senderSubsystem);
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    return getOutgoingPackage(stream, consignmentId, adressee, sender);
  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderMemberCode, String senderSubsystem)
      throws DhxException {
    InternalXroadMember adressee = addressService.getClientForMemberCode(
        recipientCode, recipientSystem);
    InternalXroadMember sender = addressService.getClientForMemberCode(
        senderMemberCode, senderSubsystem);
    return getOutgoingPackage(capsuleStream, consignmentId, adressee,
        sender);
  }

  @Loggable
  @Override
  public List<OutgoingDhxPackage> getOutgoingPackage(File capsuleFile,
      String consignmentId) throws DhxException {
    return getOutgoingPackage(capsuleFile, consignmentId,
        capsuleConfig.getCurrentCapsuleVersion());
  }

  @Loggable
  @Override
  public List<OutgoingDhxPackage> getOutgoingPackage(File capsuleFile,
      String consignmentId, CapsuleVersionEnum version)
      throws DhxException {
    InputStream stream = FileUtil.getFileAsStream(capsuleFile);
    return getOutgoingPackage(stream, consignmentId, version);
  }

  @Loggable
  @Override
  public List<OutgoingDhxPackage> getOutgoingPackage(
      InputStream capsuleStream, String consignmentId)
      throws DhxException {
    return getOutgoingPackage(capsuleStream, consignmentId,
        capsuleConfig.getCurrentCapsuleVersion());
  }

  @Loggable
  @Override
  public List<OutgoingDhxPackage> getOutgoingPackage(
      InputStream capsuleStream, String consignmentId,
      CapsuleVersionEnum version) throws DhxException {
    List<OutgoingDhxPackage> packages = new ArrayList<OutgoingDhxPackage>();
    if (version == null) {
      throw new DhxException(DhxExceptionEnum.XSD_VERSION_ERROR,
          "Unable to send document using NULL xsd version");
    }
    if (dhxConfig.getParseCapsule()) {
      InputStream schemaStream = null;
      if (dhxConfig.getCapsuleValidate()) {
        schemaStream = FileUtil.getFileAsStream(capsuleConfig
            .getXsdForVersion(version));
      }
      Object container = dhxMarshallerService.unmarshallAndValidate(
          capsuleStream, schemaStream);
      List<CapsuleAdressee> adressees = capsuleConfig
          .getAdresseesFromContainer(container);
      if (adressees != null && adressees.size() > 0) {
        File capsuleFile = null;
        capsuleFile = dhxMarshallerService.marshall(container);
        for (CapsuleAdressee adressee : adressees) {
          InternalXroadMember adresseeXroad = addressService
              .getClientForMemberCode(adressee.getAdresseeCode(),
                  null);
          OutgoingDhxPackage document = new OutgoingDhxPackage(
              adresseeXroad, soapConfig.getDefaultClient(),
              container, CapsuleVersionEnum.forClass(container
                  .getClass()),
              capsuleFile/* capsuleStream */, consignmentId,
              dhxConfig.getProtocolVersion());
          packages.add(document);
        }
        return packages;

      } else {
        throw new DhxException(
            DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
            "Container or recipient is empty. Unable to create outgoing package");
      }
    } else {
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Unable to define adressees without parsing capsule. "
              + "parsing capsule is disabled in configuration.");
    }
  }

}
