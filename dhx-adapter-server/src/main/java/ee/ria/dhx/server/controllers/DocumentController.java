package ee.ria.dhx.server.controllers;

import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.RecipientRepository;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("monitoring")
public class DocumentController {

    @Autowired
    private RecipientRepository recipientRepository;

    @Transactional
    @GetMapping(value = "/documents")
    public String getDocuments(@RequestParam(required = false, defaultValue = "101,103") List<StatusEnum> statuses,
                               @RequestParam(required = false, defaultValue = "") Boolean isOutgoing,
                               @RequestParam(required = false, defaultValue = "") String senderRegCode,
                               @RequestParam(required = false, defaultValue = "") String senderSubsystem,
                               @RequestParam(required = false, defaultValue = "") String documentTitle,
                               @DateTimeFormat(pattern = "yyyy-MM-dd")
                               @RequestParam(required = false, defaultValue = "") Date dateCreatedFrom,
                               @DateTimeFormat(pattern = "yyyy-MM-dd")
                               @RequestParam(required = false, defaultValue = "") Date dateCreatedTo,
                               @PageableDefault(size = 100) Pageable pageable,
                               Model model) {
        model.addAttribute("recipientsPage", recipientRepository.findAllBy(
                statuses,
                isOutgoing,
                senderRegCode,
                senderSubsystem,
                //documentTitle, // TODO: Uncomment if document title has been introduced
                dateCreatedFrom,
                dateCreatedTo,
                pageable
        ));
        model.addAttribute("form", new HashMap<String, Object>() {{
            put("statuses", statuses);
            put("isOutgoing", isOutgoing);
            put("senderRegCode", senderRegCode);
            put("senderSubsystem", senderSubsystem);
            put("documentTitle", documentTitle);
            put("dateCreatedFrom", dateCreatedFrom);
            put("dateCreatedTo", dateCreatedTo);
        }});
        model.addAttribute("pageable", pageable);
        return "documents-index";
    }

    @GetMapping(value = "/documents/{document_id}/recipient/{recipient_id}/status")
    public String getDocumentStatus(@PathVariable("document_id") long documentId,
                                    @PathVariable("recipient_id") long recipientId,
                                    Model model) {
        model.addAttribute("receiver", recipientRepository.findByRecipientIdAndTransportDokumentDocumentId(
                recipientId,
                documentId
        ));
        return "document-status";
    }
}
