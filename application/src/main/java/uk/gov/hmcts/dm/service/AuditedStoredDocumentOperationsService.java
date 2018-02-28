package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Created by pawel on 28/07/2017.
 */
@Transactional
@Service
public class AuditedStoredDocumentOperationsService {

    @Autowired
    private StoredDocumentService storedDocumentService;

    @Autowired
    private AuditEntryService auditEntryService;

    public List<StoredDocument> createStoredDocuments(UploadDocumentsCommand uploadDocumentsCommand) {
        List<StoredDocument> storedDocuments = storedDocumentService.saveItems(uploadDocumentsCommand);
        storedDocuments.forEach(storedDocument -> {
            auditEntryService.createAndSaveEntry(storedDocument, AuditActions.CREATED);
            auditEntryService.createAndSaveEntry(storedDocument.getDocumentContentVersions().get(0), AuditActions.CREATED);
        });
        return storedDocuments;
    }

    @PreAuthorize("hasPermission(#id, 'uk.gov.hmcts.dm.domain.StoredDocument', 'READ')")
    public StoredDocument readStoredDocument(UUID id) {
        StoredDocument storedDocument = storedDocumentService.findOne(id);

        storedDocument = createReadAuditEntry(storedDocument);

        return storedDocument;

    }

    @PreAuthorize("hasPermission(#storedDocument, 'READ')")
    private StoredDocument createReadAuditEntry(StoredDocument storedDocument) {
        if (storedDocument != null && !storedDocument.isDeleted()) {
            auditEntryService.createAndSaveEntry(storedDocument, AuditActions.READ);
            return storedDocument;
        } else {
            return null;
        }
    }

    @PreAuthorize("hasPermission(#id, 'uk.gov.hmcts.dm.domain.StoredDocument', 'UPDATE')")
    public StoredDocument updateDocument(UUID id, UpdateDocumentCommand updateDocumentCommand) {

        StoredDocument storedDocument = storedDocumentService.findOne(id);

        if (storedDocument == null || storedDocument.isDeleted()) {
            throw new StoredDocumentNotFoundException(id);
        }

        storedDocumentService.updateStoredDocument(storedDocument, updateDocumentCommand);

        auditEntryService.createAndSaveEntry(storedDocument, AuditActions.UPDATED);

        return storedDocument;
    }

    @PreAuthorize("hasPermission(#storedDocument, 'UPDATE')")
    public DocumentContentVersion addDocumentVersion(StoredDocument storedDocument, MultipartFile file) {
        DocumentContentVersion documentContentVersion =
            storedDocumentService.addStoredDocumentVersion(storedDocument, file);
        auditEntryService.createAndSaveEntry(storedDocument, AuditActions.UPDATED);
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.CREATED);

        return documentContentVersion;
    }

    @PreAuthorize("hasPermission(#id, 'uk.gov.hmcts.dm.domain.StoredDocument', 'DELETE')")
    public void deleteStoredDocument(UUID id, boolean permanent) {
        deleteStoredDocument(storedDocumentService.findOne(id), permanent);
    }

    @PreAuthorize("hasPermission(#storedDocument, 'DELETE')")
    public void deleteStoredDocument(StoredDocument storedDocument, boolean permanent) {
        if (storedDocument != null) {
            if (permanent && !storedDocument.isHardDeleted()) {
                storedDocumentService.deleteDocument(storedDocument, permanent);
                auditEntryService.createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
            } else if (!permanent && !storedDocument.isDeleted()) {
                storedDocumentService.deleteDocument(storedDocument, permanent);
                auditEntryService.createAndSaveEntry(storedDocument, AuditActions.DELETED);
            }
        }
    }

}