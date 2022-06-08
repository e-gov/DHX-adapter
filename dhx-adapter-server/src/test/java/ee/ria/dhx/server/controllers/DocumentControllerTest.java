package ee.ria.dhx.server.controllers;

import com.jcabi.aspects.Cacheable;
import ee.ria.dhx.server.config.WebMvcConfigurer;
import ee.ria.dhx.server.converters.StatusEnumConverter;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.entity.Sender;
import ee.ria.dhx.server.persistence.entity.Transport;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.RecipientRepository;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DocumentController.class, includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = StatusEnumConverter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfigurer.class)
})
@EnableSpringDataWebSupport
public class DocumentControllerTest {

    private static final List<StatusEnum> defaultStatuses = Lists.newArrayList(
            StatusEnum.IN_PROCESS, StatusEnum.FAILED
    );
    private static final String senderRegCode = "senderRegCode";
    private static final String senderSubsystem = "senderSubsystem";
    private static final String documentTitle = "documentTitle";
    private static final String dateCreatedFromStr = "2019-01-01";
    private static final Date dateCreatedFrom = Date.valueOf(dateCreatedFromStr);
    private static final String dateCreatedToStr = "2019-01-02";
    private static final Date dateCreatedTo = Date.valueOf(dateCreatedToStr);
    private static final Integer pageablePage = 2;
    private static final Integer pageableSize = 10;
    private static final String folderName = "folder";

    private Pageable pageable = PageRequest.of(pageablePage, pageableSize);

    @Mock
    Page page;
    @Mock
    Recipient recipient;
    @Mock
    Sender sender;
    @Mock
    Transport transport;
    @Mock
    Document document;
    @Mock
    Organisation organisation;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipientRepository recipientRepository;

    @SpyBean
    private DocumentController documentController;

    @Captor
    ArgumentCaptor<Model> modelArgumentCaptor;

    @Before
    public void makeThymeleafHappy() {
        when(recipient.getTransport()).thenReturn(transport);
        when(recipient.getStatusId()).thenReturn(StatusEnum.IN_PROCESS.getClassificatorId());
        when(transport.getDokument()).thenReturn(document);
        when(document.getFolder()).thenReturn(folderName);
        when(transport.getSenders()).thenReturn(Lists.newArrayList(sender));
        when(sender.getOrganisation()).thenReturn(organisation);
        when(recipient.getOrganisation()).thenReturn(organisation);
    }

    @Test
    public void getDocuments() throws Exception {
        // Prepare
        pageable = PageRequest.of(0, 100);
        when(recipientRepository.findAllBy(
                eq(defaultStatuses),
                isNull(),
                eq(StringUtils.EMPTY),
                eq(StringUtils.EMPTY),
                //eq(StringUtils.EMPTY), // TODO: Uncomment if document title has been introduced
                isNull(),
                isNull(),
                eq(pageable)
        )).thenReturn(page);
        // Test
        mockMvc.perform(get("/monitoring/documents"))
        // Verify
        .andExpect(status().isOk())
        .andExpect(model().attribute("recipientsPage", page))
        .andExpect(model().attribute("pageable", pageable))
        .andExpect(model().attributeExists("form"));
        InOrder inOrder = inOrder(recipientRepository);
        inOrder.verify(recipientRepository).findAllBy(
                eq(defaultStatuses),
                isNull(),
                eq(StringUtils.EMPTY),
                eq(StringUtils.EMPTY),
                //eq(StringUtils.EMPTY), // TODO: Uncomment if document title has been introduced
                isNull(),
                isNull(),
                eq(pageable)
        );
        inOrder.verifyNoMoreInteractions();
        assertThat((Map) viewModel().asMap().get("form"))
                .containsEntry("statuses", defaultStatuses)
                .containsEntry("isOutgoing", null)
                .containsEntry("senderRegCode", StringUtils.EMPTY)
                .containsEntry("senderSubsystem", StringUtils.EMPTY)
                .containsEntry("documentTitle", StringUtils.EMPTY)
                .containsEntry("dateCreatedFrom", null)
                .containsEntry("dateCreatedTo", null);
    }

    @Test
    public void getDocumentsWithParameters() throws Exception {
        // Prepare
        StatusEnum inProceess = StatusEnum.IN_PROCESS;
        List<StatusEnum> statuses = Lists.newArrayList(inProceess);
        when(recipientRepository.findAllBy(
                eq(statuses),
                eq(true),
                eq(senderRegCode),
                eq(senderSubsystem),
                //eq(documentTitle), // TODO: Uncomment if document title has been introduced
                eq(dateCreatedFrom),
                eq(dateCreatedTo),
                eq(pageable)
        )).thenReturn(page);
        // Test
        mockMvc.perform(get("/monitoring/documents")
                .param("statuses", inProceess.getClassificatorId().toString())
                .param("isOutgoing", "true")
                .param("senderRegCode", senderRegCode)
                .param("senderSubsystem", senderSubsystem)
                //.param("documentTitle", documentTitle) // TODO: Uncomment if document title has been introduced
                .param("dateCreatedFrom", dateCreatedFromStr)
                .param("dateCreatedTo", dateCreatedToStr)
                .param("page", pageablePage.toString())
                .param("size", pageableSize.toString()))
        // Verify
        .andExpect(status().isOk())
        .andExpect(model().attribute("recipientsPage", page))
        .andExpect(model().attribute("pageable", pageable))
        .andExpect(model().attributeExists("form"));
        InOrder inOrder = inOrder(recipientRepository);
        inOrder.verify(recipientRepository).findAllBy(
                eq(statuses),
                eq(true),
                eq(senderRegCode),
                eq(senderSubsystem),
                //eq(documentTitle), // TODO: Uncomment if document title has been introduced
                eq(dateCreatedFrom),
                eq(dateCreatedTo),
                eq(pageable)
        );
        inOrder.verifyNoMoreInteractions();
        assertThat((Map) viewModel().asMap().get("form"))
                .containsEntry("statuses", statuses)
                .containsEntry("isOutgoing", true)
                .containsEntry("senderRegCode", senderRegCode)
                .containsEntry("senderSubsystem", senderSubsystem)
                //.containsEntry("documentTitle", documentTitle) // TODO: Uncomment if document title has been introduced
                .containsEntry("dateCreatedFrom", dateCreatedFrom)
                .containsEntry("dateCreatedTo", dateCreatedTo);
    }

    @Cacheable(forever = true)
    private Model viewModel() {
        verify(documentController).getDocuments(any(), any(), any(), any(), any(), any(), any(), any(), modelArgumentCaptor.capture());
        return modelArgumentCaptor.getValue();
    }

    @Test
    public void getDocumentStatus() throws Exception {
        // Prepare
        long documentId = 11;
        long recipientId = 22;
        when(recipientRepository.findByRecipientIdAndTransportDokumentDocumentId(eq(recipientId), eq(documentId)))
                .thenReturn(recipient);
        // Test
        mockMvc.perform(get("/monitoring/documents/{document_id}/recipient/{recipient_id}/status", documentId, recipientId))
        // Verify
        .andExpect(status().isOk())
        .andExpect(model().attribute("receiver", recipient));
        InOrder inOrder = inOrder(recipientRepository);
        inOrder.verify(recipientRepository).findByRecipientIdAndTransportDokumentDocumentId(
                eq(recipientId),
                eq(documentId)
        );
        inOrder.verifyNoMoreInteractions();
    }

}
