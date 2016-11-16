package ee.bpw.dhx.server.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.IncomingDhxPackage;
import ee.bpw.dhx.model.DhxRepresentee;
import ee.bpw.dhx.ws.service.impl.ExampleDhxImplementationSpecificService;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DhxAdapterServerSpecificService extends ExampleDhxImplementationSpecificService {
 

}
