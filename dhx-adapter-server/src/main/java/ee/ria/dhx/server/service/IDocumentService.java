package ee.ria.dhx.server.service;

import java.util.List;

public interface IDocumentService {
    List<DocumentStatus> findAll();
}
