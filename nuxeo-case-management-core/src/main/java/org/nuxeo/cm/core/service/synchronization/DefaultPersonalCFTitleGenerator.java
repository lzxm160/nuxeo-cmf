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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.cm.core.service.synchronization;

import org.nuxeo.cm.service.CaseFolderTitleGenerator;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;


public class DefaultPersonalCFTitleGenerator implements CaseFolderTitleGenerator {

    public String getCaseFolderTitle(DocumentModel directoryEntry) throws PropertyException, ClientException {
        String firstName = (String) directoryEntry.getPropertyValue("user:firstName");
        String lastName = (String) directoryEntry.getPropertyValue("user:lastName");
        String company = (String) directoryEntry.getPropertyValue("user:company");
        return String.format("%s %s (%s)", firstName, lastName, company);
    }

}