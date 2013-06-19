
package org.activiti.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.message.SearchResponse;
import org.apache.directory.ldap.client.api.message.SearchResultEntry;
import org.apache.directory.shared.ldap.cursor.Cursor;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.filter.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLDAPManager
{
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final LDAPConnectionParams connectionParams;

    protected AbstractLDAPManager(final LDAPConnectionParams params)
    {
        connectionParams = params;
    }

    protected String getUserDn(final String userId)
    {
        final String cn = getUserCn(userId);

        return cn == null ? null : "cn=" + cn + "," + connectionParams.getLdapUserBase();
    }

    protected String getUserCn(final String userId)
    {
        if (connectionParams.isCommonNameUserId())
        {
            return userId;
        }

        final LDAPUserEntity user = (LDAPUserEntity) findUserById(userId);

        if (user == null)
        {
            return null;
        }

        return user.getCommonName();
    }

    public UserEntity findUserById(final String userId)
    {
        LDAPUserEntity user = null;
        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);

        try
        {
            final String filter = "(&(" + connectionParams.getLdapUserIdAttribute() + "=" + userId
                                  + ")(objectclass=" + connectionParams.getLdapUserObject() + "))";

            LOGGER.debug("findUserById: " + filter);

            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapUserBase(),
                filter, SearchScope.ONELEVEL, "*");

            if (cursor.next())
            {
                user = new LDAPUserEntity();

                final SearchResultEntry response = (SearchResultEntry) cursor.get();
                final Iterator<EntryAttribute> itEntry = response.getEntry().iterator();

                while (itEntry.hasNext())
                {
                    final EntryAttribute attribute = itEntry.next();
                    setUserAttribute(attribute, user);
                }
            }

            cursor.close();
        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection search failure", e);
        }

        LDAPConnectionUtil.closeConnection(connection);

        return user;
    }

    protected void setUserAttribute(final EntryAttribute attribute, final LDAPUserEntity user)
        throws InvalidAttributeValueException
    {
        final String key = attribute.getId();
        if (connectionParams.getLdapUserIdAttribute().equalsIgnoreCase(key))
        {
            user.setId(attribute.getString());
        }
        else if ("sn".equalsIgnoreCase(key))
        {
            user.setLastName(attribute.getString());
        }
        else if ("givenName".equalsIgnoreCase(key))
        {
            user.setFirstName(attribute.getString());
        }
        else if ("mail".equalsIgnoreCase(key))
        {
            user.setEmail(attribute.getString());
        }
        else if ("cn".equalsIgnoreCase(key))
        {
            user.setCommonName(attribute.getString());
        }
    }

    public List<Group> findGroupsByUser(final String userId)
    {
        final List<Group> groupList = new ArrayList<Group>();

        final String userCn = getUserCn(userId);
        if (userCn == null)
        {
            return groupList;
        }

        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            final String filter = "(&(" + connectionParams.getLdapGroupMemberAttribute() + "=" + userCn
                                  + ")(objectclass=" + connectionParams.getLdapGroupObject() + "))";

            LOGGER.debug("findGroupsByUser: " + filter);

            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapGroupBase(),
                filter, SearchScope.ONELEVEL, "*");

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

    protected void setGroupAttributes(final Group group, final EntryAttribute attribute)
        throws InvalidAttributeValueException
    {
        final String key = attribute.getId();
        if ("cn".equalsIgnoreCase(key))
        {
            final String groupCN = attribute.getString();
            group.setName(groupCN);

            if (connectionParams.getActivitiUserGroupCns().contains(groupCN))
            {
                group.setId(LDAPConnectionParams.ACTIVITI_SECURITY_ROLE_USER);
                group.setType(LDAPConnectionParams.ACTIVITI_SECURITY_ROLE);
            }
            else if (connectionParams.getActivitiAdminGroupCns().contains(groupCN))
            {
                group.setId(LDAPConnectionParams.ACTIVITI_SECURITY_ROLE_ADMIN);
                group.setType(LDAPConnectionParams.ACTIVITI_SECURITY_ROLE);
            }
            else
            {
                group.setId(groupCN);
                group.setType("assignment");
            }
        }
    }
}
