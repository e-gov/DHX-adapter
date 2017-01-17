package ee.ria.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;

import lombok.Setter;
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
  @Setter
  SoapConfig soapConfig;

  @Autowired
  @Setter
  AddressService addressService;

  @Autowired
  @Setter
  CapsuleConfig capsuleConfig;

  @Autowired
  @Setter
  DhxConfig dhxConfig;

  @Autowired
  @Setter
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
    InputStream schemaStream = null;
    OutgoingDhxPackage document = null;
    try {
      schemaStream = FileUtil
          .getFileAsStream(capsuleConfig
              .getXsdForVersion(capsuleConfig
                  .getCurrentCapsuleVersion()));
      document = getOutgoingPackage(capsuleFile, consignmentId,
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
      InternalXroadMember sender) throws DhxException {
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
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, InternalXroadMember recipient,
      InternalXroadMember sender, InputStream schemaStream,
      String dhxProtocolVersion) throws DhxException {
    OutgoingDhxPackage document = new OutgoingDhxPackage(recipient, sender,
        FileUtil.getDatahandlerFromFile(capsuleFile), consignmentId, dhxProtocolVersion);
    InputStream fileStream = null;
    try {
      if (dhxConfig.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        fileStream = document.getDocumentFile()
            .getInputStream();
        dhxMarshallerService.validate(fileStream, schemaStream);
      } else {
        log.debug("Validating capsule is disabled");
      }
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.WS_ERROR,
          "Error occured while reading or writing capsule file.", ex);
    }finally {
      FileUtil.safeCloseStream(fileStream);
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
        FileUtil.getDatahandlerFromStream(capsuleStream), consignmentId, dhxProtocolVersion);
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
        sender, schemaStream, dhxProtocolVersion);
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
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem)
      throws DhxException {
    return getOutgoingPackage(capsuleStream, consignmentId, recipientCode,
        recipientSystem, null);

  }

  @Loggable
  @Override
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderSubsystem) throws DhxException {
    InternalXroadMember adressee = addressService.getClientForMemberCode(
        recipientCode, recipientSystem);
    InternalXroadMember sender = soapConfig.getDefaultClient();
    if (senderSubsystem != null) {
      sender.setSubsystemCode(senderSubsystem);
    }
    return getOutgoingPackage(capsuleFile, consignmentId, adressee,
        sender);
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
    return getOutgoingPackage(capsuleFile, consignmentId, adressee, sender);
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
  public List<OutgoingDhxPackage> getOutgoingPackage(
      File capsuleFile, String consignmentId,
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
          capsuleFile, schemaStream);
      List<CapsuleAdressee> adressees = capsuleConfig
          .getAdresseesFromContainer(container);
      if (adressees != null && adressees.size() > 0) {
        /*
         * File capsuleFile = null; capsuleFile = dhxMarshallerService.marshall(container);
         */
        for (CapsuleAdressee adressee : adressees) {
          InternalXroadMember adresseeXroad = addressService
              .getClientForMemberCode(adressee.getAdresseeCode(),
                  null);
          OutgoingDhxPackage document = new OutgoingDhxPackage(
              adresseeXroad, soapConfig.getDefaultClient(),
              container, CapsuleVersionEnum.forClass(container
                  .getClass()),
              FileUtil.getDatahandlerFromFile(capsuleFile), consignmentId,
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
              FileUtil.getDatahandlerFromFile(capsuleFile), consignmentId,
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
