package ee.ria.dhx.server.controllers;

import ee.ria.dhx.server.service.DocumentStatus;
import ee.ria.dhx.server.service.IDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class DocumentController {
    @Autowired
    private IDocumentService documentService;

    @RequestMapping(value = "/document-index")
    public String getDocuments(Model model) {
        List<DocumentStatus> documentStatuses = documentService.findAll();
        model.addAttribute("document-statuses", documentStatuses);
        return "document-index";
    }
}
