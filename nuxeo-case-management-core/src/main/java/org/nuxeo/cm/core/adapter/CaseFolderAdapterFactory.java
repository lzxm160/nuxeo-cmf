/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Alexandre Russel
 *
 * $Id$
 */

package org.nuxeo.cm.core.adapter;

import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.casefolder.CaseFolderImpl;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;


/**
 * @author arussel
 *
 */
public class CaseFolderAdapterFactory implements DocumentAdapterFactory {

    @SuppressWarnings("unchecked")
    public Object getAdapter(DocumentModel doc, Class arg1) {
        if (checkDocument(doc)) {
            return new CaseFolderImpl(doc);
        } else {
            // cannot be adapted
            return null;
        }
    }

    /**
     * Returns false if document does not have
     * {@link CaseFolderConstants#CASE_FOLDER_FACET}
     *
     * @param doc
     * @throws ClientRuntimeException if document has the required facet but
     *             does not have the required
     *             {@link CaseFolderConstants#CASE_FOLDER_SCHEMA}
     */
    public static boolean checkDocument(DocumentModel doc) {
        if (!doc.hasFacet(CaseFolderConstants.CASE_FOLDER_FACET)) {
            // not a mailbox
            return false;
        } else if (!doc.hasSchema(CaseFolderConstants.CASE_FOLDER_SCHEMA)) {
            throw new CaseManagementRuntimeException(
                    "Document should contain schema "
                            + CaseFolderConstants.CASE_FOLDER_SCHEMA);
        }
        return true;
    }
}
