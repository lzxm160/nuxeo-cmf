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
 *     Anahide Tchertchian
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.casefolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Type;

/**
 * Mailbox implementation using a document model as backend
 *
 * @author Anahide Tchertchian
 *
 */
public class CaseFolderImpl implements CaseFolder {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseFolder.class);

    protected DocumentModel doc;

    public CaseFolderImpl(DocumentModel doc) {
        this.doc = doc;
    }

    public DocumentModel getDocument() {
        return doc;
    }

    protected String getStringProperty(String property) {
        try {
            return (String) doc.getPropertyValue(property);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<String> getStringListProperty(String property) {
        try {
            List<String> res = null;
            Object propValue = doc.getPropertyValue(property);
            if (propValue instanceof List) {
                res = (List<String>) propValue;
            } else if (propValue instanceof String[]) {
                res = Arrays.asList((String[]) propValue);
            } else if (propValue != null) {
                throw new ClientRuntimeException(String.format(
                        "Unexpected non-list value for prop %s: %s", property,
                        propValue));
            }
            return res;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected Integer getIntegerProperty(String property) {
        try {
            Object value = doc.getPropertyValue(property);
            if (value instanceof Long) {
                return Integer.valueOf(((Long) value).toString());
            }
            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected void setPropertyValue(String property, Serializable value) {
        try {
            doc.setPropertyValue(property, value);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public ParticipantsList getParticipantListTemplate() {
        try {
            Object value = null;
            Property prop = doc.getProperty(CaseFolderConstants.PARTICIPANTS_LIST_FIELD);
            Field field = prop.getField();
            if (field != null) {
                Type type = field.getType();
                if (type.isListType()) {
                    Type itemType = ((ListType) type).getFieldType();
                    value = itemType.newInstance();
                }
            }
            if (!(value instanceof Map)) {
                throw new ClientRuntimeException(
                        "Cannot get default template for mailing list");
            }
            Map<String, Serializable> map = (Map<String, Serializable>) value;
            return new ParticipantListImpl(map);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void addParticipantList(ParticipantsList ml) {
        try {
            ArrayList<Map<String, Serializable>> mls = new ArrayList<Map<String, Serializable>>();
            List<Map<String, Serializable>> mailinglists = (List<Map<String, Serializable>>) doc.getPropertyValue(CaseFolderConstants.PARTICIPANTS_LIST_FIELD);
            if (mailinglists != null) {
                mls.addAll(mailinglists);
            }
            mls.add(ml.getMap());
            setPropertyValue(CaseFolderConstants.PARTICIPANTS_LIST_FIELD, mls);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    public List<String> getUsers() {
        return getStringListProperty(CaseFolderConstants.USERS_FIELD);
    }

    public String getDescription() {
        return getStringProperty(CaseFolderConstants.DESCRIPTION_FIELD);
    }

    public List<String> getFavorites() throws CaseManagementException {
        return getStringListProperty(CaseFolderConstants.FAVORITES_FIELD);
    }

    public List<String> getGroups() {
        return getStringListProperty(CaseFolderConstants.GROUPS_FIELD);
    }

    public String getId() {
        return getStringProperty(CaseFolderConstants.ID_FIELD);
    }

    public void setId(String id) {
        setPropertyValue(CaseFolderConstants.ID_FIELD, id);
    }

    public List<String> getParticipantListIds() {
        List<String> mlids = new ArrayList<String>();
        List<ParticipantsList> mls = getParticipantLists();
        if (mls != null) {
            for (ParticipantsList ml : mls) {
                mlids.add(ml.getId());
            }
        }
        return mlids;
    }

    @SuppressWarnings("unchecked")
    public List<ParticipantsList> getParticipantLists() {
        try {
            List<ParticipantsList> mls = new ArrayList<ParticipantsList>();
            List<Map<String, Serializable>> mailinglists = (List<Map<String, Serializable>>) doc.getPropertyValue(CaseFolderConstants.PARTICIPANTS_LIST_FIELD);
            if (mailinglists != null) {
                for (Map<String, Serializable> mailinglist : mailinglists) {
                    mls.add(new ParticipantListImpl(mailinglist));
                }
            }
            return mls;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    public List<String> getNotifiedUsers() {
        return getStringListProperty(CaseFolderConstants.NOTIFIED_USERS_FIELD);
    }

    public String getOwner() {
        return getStringProperty(CaseFolderConstants.OWNER_FIELD);
    }

    public List<String> getProfiles() {
        return getStringListProperty(CaseFolderConstants.PROFILES_FIELD);
    }

    public String getTitle() {
        return getStringProperty(CaseFolderConstants.TITLE_FIELD);
    }

    public String getType() {
        return getStringProperty(CaseFolderConstants.TYPE_FIELD);
    }

    public List<String> getAllUsers() {
        ArrayList<String> delegates = new ArrayList<String>();
        List<String> users = getUsers();
        if (users != null) {
            delegates.addAll(users);
        }
        String owner = getOwner();
        if (owner != null && !delegates.contains(owner)) {
            delegates.add(0, owner);
        }
        return delegates;
    }

    public boolean hasProfile(String profile) {
        List<String> profiles = getProfiles();
        if (profiles != null && profiles.contains(profile)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void removeParticipantList(String mailingListId) {
        try {
            boolean set = false;
            ArrayList<Map<String, Serializable>> mls = new ArrayList<Map<String, Serializable>>();
            List<Map<String, Serializable>> mailinglists = (List<Map<String, Serializable>>) doc.getPropertyValue(CaseFolderConstants.PARTICIPANTS_LIST_FIELD);
            if (mailinglists != null) {
                for (Map<String, Serializable> ml : mailinglists) {
                    if (mailingListId.equals(ml.get(CaseFolderConstants.PARTICIPANTLIST_ID_FIELD))) {
                        set = true;
                    } else {
                        mls.add(ml);
                    }
                }
            }
            if (set) {
                setPropertyValue(CaseFolderConstants.PARTICIPANTS_LIST_FIELD,
                        mls);
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    public void setUsers(List<String> users) {
        ArrayList<String> serializableUsers = new ArrayList<String>();
        if (users != null) {
            serializableUsers.addAll(users);
        }
        setPropertyValue(CaseFolderConstants.USERS_FIELD, serializableUsers);
    }

    public void setDescription(String description) {
        setPropertyValue(CaseFolderConstants.DESCRIPTION_FIELD, description);
    }

    public void setFavorites(List<String> favorites)
            throws CaseManagementException {
        ArrayList<String> serializableFavorites = new ArrayList<String>();
        if (favorites != null) {
            serializableFavorites.addAll(favorites);
        }
        setPropertyValue(CaseFolderConstants.FAVORITES_FIELD,
                serializableFavorites);
    }

    public void setGroups(List<String> groups) {
        ArrayList<String> serializableGroups = new ArrayList<String>();
        if (groups != null) {
            serializableGroups.addAll(groups);
        }
        setPropertyValue(CaseFolderConstants.GROUPS_FIELD, serializableGroups);
    }

    public void setNotifiedUsers(List<String> users) {
        ArrayList<String> serializableUsers = new ArrayList<String>();
        if (users != null) {
            serializableUsers.addAll(users);
        }
        setPropertyValue(CaseFolderConstants.NOTIFIED_USERS_FIELD,
                serializableUsers);
    }

    public void setOwner(String owner) {
        setPropertyValue(CaseFolderConstants.OWNER_FIELD, owner);
    }

    public void setProfiles(List<String> profiles) {
        ArrayList<String> serializableProfiles = new ArrayList<String>();
        if (profiles != null) {
            serializableProfiles.addAll(profiles);
        }
        setPropertyValue(CaseFolderConstants.PROFILES_FIELD,
                serializableProfiles);
    }

    public void setTitle(String title) {
        setPropertyValue(CaseFolderConstants.TITLE_FIELD, title);
    }

    public void setType(String type) {
        setPropertyValue(CaseFolderConstants.TYPE_FIELD, type);
    }

    public void setConfidentiality(Integer confidentiality) {
        setPropertyValue(CaseFolderConstants.CONFIDENTIALITY_FIELD,
                confidentiality);
    }

    public Integer getConfidentiality() {
        return getIntegerProperty(CaseFolderConstants.CONFIDENTIALITY_FIELD);
    }

    public int compareTo(CaseFolder other) {
        // sort by type and then by title
        if (getType().equals(other.getType())) {
            // sort by title
            return getTitle().compareTo(other.getTitle());
        } else if (CaseFolderConstants.type.personal.name().equals(getType())) {
            return -1;
        } else {
            return 1;
        }
    }

    public void save(CoreSession session) {
        try {
            session.saveDocument(doc);
            session.save();
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public String getParentId(CoreSession session) {
        try {
            if (session.hasPermission(doc.getParentRef(),
                    SecurityConstants.READ)) {
                DocumentModel parent = session.getDocument(doc.getParentRef());
                CaseFolder parentMailbox = parent.getAdapter(CaseFolder.class);
                if (parentMailbox != null) {
                    return parentMailbox.getId();
                }
            }
        } catch (ClientException e) {
            log.error("Unable to retrieve parent case folder id", e);
        }
        return null;
    }

    public List<String> getChildrenIds(CoreSession session) {
        List<String> res = new ArrayList<String>();
        try {
            if (session.hasPermission(doc.getRef(),
                    SecurityConstants.READ_CHILDREN)) {
                DocumentModelList children = session.getChildren(doc.getRef());
                if (children != null) {
                    for (DocumentModel child : children) {
                        CaseFolder childCaseFolder = child.getAdapter(CaseFolder.class);
                        if (childCaseFolder != null) {
                            res.add(childCaseFolder.getId());
                        }
                    }
                }
            }
        } catch (ClientException e) {
            log.error("Unable to retrieve child case folder ids", e);
        }
        return res;
    }

    public List<String> getAllUsersAndGroups() {
        List<String> total = new ArrayList<String>();
        List<String> users = getAllUsers();
        if (users != null) {
            total.addAll(users);
        }

        List<String> groups = getGroups();
        if (groups != null) {
            total.addAll(groups);
        }
        return total;
    }

    public String getAffiliatedCaseFolderId() {
        return getStringProperty(CaseFolderConstants.AFFILIATED_CASE_FOLDER_ID);
    }
}