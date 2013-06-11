
package org.activiti.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.message.SearchResponse;
import org.apache.directory.ldap.client.api.message.SearchResultEntry;
import org.apache.directory.shared.ldap.cursor.Cursor;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.filter.SearchScope;

public class LDAPGroupManager extends GroupEntityManager
{
    private static final Log LOG = LogFactory.getLog(LDAPGroupManager.class);

    private final LDAPConnectionParams connectionParams;

    public LDAPGroupManager(final LDAPConnectionParams params)
    {
        this.connectionParams = params;
    }

    @Override
    public Group createNewGroup(final String groupId)
    {
        throw new ActivitiException("LDAP group manager doesn't support creating a new group");
    }

    @Override
    public void insertGroup(final Group group)
    {
        throw new ActivitiException("LDAP group manager doesn't support inserting a new group");
    }

    @Override
    public void deleteGroup(final String groupId)
    {
        throw new ActivitiException("LDAP group manager doesn't support deleting a new group");
    }

    @Override
    public List<Group> findGroupByQueryCriteria(final GroupQueryImpl query, final Page page)
    {
        LOG.debug("findGroupByQueryCriteria");

        final List<Group> groupList = new ArrayList<Group>();

        // Query is a GroupQueryImpl instance
        final GroupQueryImpl groupQuery = query;
        final StringBuilder searchQuery = new StringBuilder();
        if (StringUtils.isNotEmpty(groupQuery.getId()))
        {
            searchQuery.append("(&(cn=")
                .append(groupQuery.getId())
                .append(")(objectclass=" + connectionParams.getLdapGroupObject() + "))");

        }
        else if (StringUtils.isNotEmpty(groupQuery.getName()))
        {
            searchQuery.append("(&(cn=")
                .append(groupQuery.getName())
                .append(")(objectclass=" + connectionParams.getLdapGroupObject() + "))");

        }
        else if (StringUtils.isNotEmpty(groupQuery.getUserId()))
        {
            searchQuery.append("(&(member= cn=")
                .append(groupQuery.getUserId())
                .append(
                    "," + connectionParams.getLdapUserBase() + ")(objectclass="
                                    + connectionParams.getLdapGroupObject() + "))");
        }
        else
        {
            searchQuery.append("(&(cn=*)(objectclass=" + connectionParams.getLdapGroupObject() + "))");
        }

        LOG.debug("searchQuery: " + searchQuery.toString());

        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapGroupBase(),
                searchQuery.toString(), SearchScope.ONELEVEL, "*");
            while (cursor.next())
            {
                final Group group = new GroupEntity();
                final SearchResultEntry response = (SearchResultEntry) cursor.get();
                final Iterator<EntryAttribute> itEntry = response.getEntry().iterator();
                while (itEntry.hasNext())
                {
                    final EntryAttribute attribute = itEntry.next();
                    final String key = attribute.getId();
                    if ("cn".equalsIgnoreCase(key))
                    {
                        // LOG.debug("atrribute: "+attribute.getString());
                        group.setId(attribute.getString());
                        group.setName(attribute.getString());
                        if (attribute.getString().equalsIgnoreCase("user")
                            || attribute.getString().equalsIgnoreCase("admin"))
                        {
                            group.setType("security-role");
                        }
                        else
                        {
                            group.setType("assignment");
                        }

                    }
                }

                groupList.add(group);
            }

            cursor.close();

        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection search failure", e);
        }

        LDAPConnectionUtil.closeConnection(connection);

        return groupList;
    }

    @Override
    public long findGroupCountByQueryCriteria(final GroupQueryImpl query)
    {
        LOG.debug("findGroupCountByQueryCriteria");
        return findGroupByQueryCriteria(query, null).size();
    }

    @Override
    public GroupEntity findGroupById(final String groupId)
    {
        LOG.debug("findGroupById: " + groupId);

        final GroupEntity group = new GroupEntity();
        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapGroupBase(),
                "(&(cn=" + groupId + "," + connectionParams.getLdapUserBase() + ")(objectclass="
                                + connectionParams.getLdapGroupObject() + "))", SearchScope.ONELEVEL, "*");
            while (cursor.next())
            {
                final SearchResultEntry response = (SearchResultEntry) cursor.get();
                final Iterator<EntryAttribute> itEntry = response.getEntry().iterator();
                while (itEntry.hasNext())
                {
                    final EntryAttribute attribute = itEntry.next();
                    setGroupAttributes(group, attribute);
                }
                break;
            }

            cursor.close();

        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection search failure", e);
        }

        LDAPConnectionUtil.closeConnection(connection);

        return group;

    }

    @Override
    public List<Group> findGroupsByUser(final String userId)
    {
        LOG.debug("findGroupsByUser for user: " + userId);
        final List<Group> groupList = new ArrayList<Group>();

        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            LOG.debug("search: " + "(member= cn=" + userId + "," + connectionParams.getLdapUserBase() + ")");

            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapGroupBase(),
                "(&(member= cn=" + userId + "," + connectionParams.getLdapUserBase() + ")(objectclass="
                                + connectionParams.getLdapGroupObject() + "))", SearchScope.ONELEVEL, "*");
            while (cursor.next())
            {
                final Group group = new GroupEntity();
                final SearchResultEntry response = (SearchResultEntry) cursor.get();
                final Iterator<EntryAttribute> itEntry = response.getEntry().iterator();
                while (itEntry.hasNext())
                {
                    final EntryAttribute attribute = itEntry.next();
                    setGroupAttributes(group, attribute);
                }

                groupList.add(group);
            }

            cursor.close();

        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection search failure", e);
        }

        LDAPConnectionUtil.closeConnection(connection);

        return groupList;
    }

    private void setGroupAttributes(final Group group, final EntryAttribute attribute)
        throws InvalidAttributeValueException
    {
        final String key = attribute.getId();
        if ("cn".equalsIgnoreCase(key))
        {
            group.setId(attribute.getString());
            group.setName(attribute.getString());
            if (attribute.getString().equalsIgnoreCase("user")
                || attribute.getString().equalsIgnoreCase("admin"))
            {
                group.setType("security-role");
            }
            else
            {
                group.setType("assignment");
            }
        }
    }
}
