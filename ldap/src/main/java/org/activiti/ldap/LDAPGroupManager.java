
package org.activiti.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.message.SearchResponse;
import org.apache.directory.ldap.client.api.message.SearchResultEntry;
import org.apache.directory.shared.ldap.cursor.Cursor;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.filter.SearchScope;

public class LDAPGroupManager extends AbstractLDAPManager implements GroupIdentityManager, Session
{
    public LDAPGroupManager(final LDAPConnectionParams params)
    {
        super(params);
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
    public void updateGroup(final GroupEntity updatedGroup)
    {
        throw new ActivitiException("LDAP group manager doesn't support updating a group");
    }

    @Override
    public void deleteGroup(final String groupId)
    {
        throw new ActivitiException("LDAP group manager doesn't support deleting a new group");
    }

    @Override
    public void flush()
    {
        // NOOP
    }

    @Override
    public void close()
    {
        // NOOP
    }

    @Override
    public GroupQuery createNewGroupQuery()
    {
        return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    @Override
    public List<Group> findGroupsByNativeQuery(final Map<String, Object> parameterMap,
                                               final int firstResult,
                                               final int maxResults)
    {
        throw new ActivitiException("LDAP group manager doesn't support finding groups by native query: "
                                    + parameterMap);
    }

    @Override
    public long findGroupCountByNativeQuery(final Map<String, Object> parameterMap)
    {
        return findGroupsByNativeQuery(parameterMap, 0, -1).size();
    }

    @Override
    public List<Group> findGroupByQueryCriteria(final GroupQueryImpl query, final Page page)
    {
        final List<Group> groupList = new ArrayList<Group>();

        // Query is a GroupQueryImpl instance
        final GroupQueryImpl groupQuery = query;
        final StringBuilder filter = new StringBuilder("(&");

        if (StringUtils.isNotEmpty(groupQuery.getId()))
        {
            filter.append("(cn=").append(groupQuery.getId()).append(")");

        }
        else if (StringUtils.isNotEmpty(groupQuery.getName()))
        {
            filter.append("(cn=").append(groupQuery.getName()).append(")");

        }
        else if (StringUtils.isNotEmpty(groupQuery.getUserId()))
        {
            final String userCn = getUserCn(groupQuery.getUserId());

            if (userCn == null)
            {
                return groupList;
            }

            filter.append("(")
                .append(connectionParams.getLdapGroupMemberAttribute())
                .append("=")
                .append(userCn)
                .append(")");
        }
        else
        {
            filter.append("(cn=*)");
        }

        filter.append("(objectclass=").append(connectionParams.getLdapGroupObject()).append("))");

        LOGGER.debug("findGroupByQueryCriteria: " + filter.toString());

        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapGroupBase(),
                filter.toString(), SearchScope.ONELEVEL, "*");

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

    @Override
    public long findGroupCountByQueryCriteria(final GroupQueryImpl query)
    {
        LOGGER.debug("findGroupCountByQueryCriteria");
        return findGroupByQueryCriteria(query, null).size();
    }
}
