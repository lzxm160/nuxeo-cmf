/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     mcedica
 */
package org.nuxeo.cm.core.service.importer;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.GetParentPathUnrestricted;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.importer.factories.DefaultDocumentModelFactory;
import org.nuxeo.ecm.platform.importer.source.SourceNode;
import org.nuxeo.runtime.api.Framework;


/**
*
* Implementation for CaseManagement factory; each time a file is found
* a new caseItem is created  and the corresponding case; the case is sent
* to the specified destionationCaseFolder
*
* @author Mariana Cedica
*
*/
public class CaseManagementCaseItemDocumentFactory extends
        DefaultDocumentModelFactory {

    private String destionationCaseFolderPath;

    private CaseDistributionService caseDistributionService;

    private CaseManagementDocumentTypeService caseManagementDocumentTypeService;

    @Override
    public DocumentModel createFolderishNode(CoreSession session,
            DocumentModel parent, SourceNode node) throws Exception {
        return null;
    }

    @Override
    public DocumentModel createLeafNode(CoreSession session,
            DocumentModel parent, SourceNode node) throws Exception {
        return createCaseItemInCase(session, node);
    }

    protected DocumentModel createCaseItemInCase(CoreSession session,
            SourceNode node) throws Exception {
        caseDistributionService = getCaseDistributionService();
        if (caseDistributionService == null) {
            return null;
        }
        String caseRootPath = getCaseRootPath(session);
        DocumentModel caseItemDoc = defaultCreateNodeDoc(session, caseRootPath,
                node, getCaseManagementDocumentTypeService().getCaseItemType());
        Case caseDoc = caseDistributionService.createCase(session, caseItemDoc,
                getCaseRootPath(session),
                Collections.singletonList(getDestinationCaseFolder(session)));
        // Retrieve the new created caseItem doc in order to set properties on
        // it
        caseItemDoc = caseDoc.getFirstItem(session).getDocument();
        setPropertiesOnImport(session, caseItemDoc, caseDoc);
        // create the corresponding caseLink in the receiver caseFolder
        caseDistributionService.createDraftCaseLink(session,
                getDestinationCaseFolder(session), caseDoc);
        return caseItemDoc;
    }

    protected DocumentModel defaultCreateNodeDoc(CoreSession session,
            String parentPath, SourceNode node, String docType)
            throws Exception {

        BlobHolder bh = node.getBlobHolder();
        String mimeType = bh.getBlob().getMimeType();
        if (mimeType == null) {
            mimeType = getMimeType(node.getName());
        }

        String name = getValidNameFromFileName(node.getName());
        String fileName = node.getName();

        Map<String, Object> options = new HashMap<String, Object>();
        DocumentModel doc = session.createDocumentModel(docType, options);
        doc.setPathInfo(parentPath, name);
        doc.setProperty("dublincore", "title", node.getName());
        doc.setProperty("file", "filename", fileName);
        doc.setProperty("file", "content", bh.getBlob());

        Map<String, Serializable> props = bh.getProperties();
        if (props != null) {
            for (String pName : props.keySet()) {
                doc.setPropertyValue(pName, props.get(pName));
            }
        }
        return doc;
    }

    private void setPropertiesOnImport(CoreSession session,
            DocumentModel caseItemDoc, Case caseDoc) throws Exception {
        caseItemDoc.setPropertyValue(
                CaseConstants.DOCUMENT_DEFAULT_CASE_ID_PROPERTY_NAME,
                caseDoc.getDocument().getId());
        caseItemDoc.setPropertyValue(
                CaseConstants.DOCUMENT_IMPORT_DATE_PROPERTY_NAME,
                Calendar.getInstance());
        // TODO : check if we need to set other properties like origin..etc
        session.saveDocument(caseItemDoc);
    }

    private String getCaseRootPath(CoreSession session) throws ClientException {
        GetParentPathUnrestricted runner = new GetParentPathUnrestricted(
                session);
        runner.runUnrestricted();
        return runner.getParentPath();
    }

    private CaseDistributionService getCaseDistributionService()
            throws Exception {
        if (caseDistributionService == null) {
            caseDistributionService = Framework.getService(CaseDistributionService.class);
        }
        return caseDistributionService;
    }

    private CaseFolder getDestinationCaseFolder(CoreSession session)
            throws ClientException {
        DocumentModel docDestinationCaseFolder = session.getDocument(new PathRef(
                destionationCaseFolderPath));
        return docDestinationCaseFolder.getAdapter(CaseFolder.class);
    }

    private CaseManagementDocumentTypeService getCaseManagementDocumentTypeService()
            throws Exception {
        if (caseManagementDocumentTypeService == null) {
            caseManagementDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        }
        return caseManagementDocumentTypeService;
    }

    public String getDestionationCaseFolderPath() {
        return destionationCaseFolderPath;
    }

    public void setDestionationCaseFolderPath(String destionationCaseFolderPath) {
        this.destionationCaseFolderPath = destionationCaseFolderPath;
    }

}