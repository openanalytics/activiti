
package org.activiti.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.message.BindResponse;
import org.apache.directory.ldap.client.api.message.SearchResponse;
import org.apache.directory.ldap.client.api.message.SearchResultEntry;
import org.apache.directory.shared.ldap.cursor.Cursor;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.filter.SearchScope;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;

public class LDAPUserManager extends UserEntityManager
{
    private static final Log LOG = LogFactory.getLog(LDAPUserManager.class);

    private final LDAPConnectionParams connectionParams;

    public LDAPUserManager(final LDAPConnectionParams params)
    {
        this.connectionParams = params;
    }

    @Override
    public User createNewUser(final String userId)
    {
        throw new ActivitiException("LDAP user manager doesn't support creating a new user");
    }

    @Override
    public void insertUser(final User user)
    {
        throw new ActivitiException("LDAP user manager doesn't support inserting a new user");
    }

    @Override
    public UserEntity findUserById(final String userId)
    {
        LOG.debug("findUserById: " + userId);
        final UserEntity user = new UserEntity();
        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            LOG.debug("search: " + "(&(cn=" + userId + ")");

            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapGroupBase(),
                "(&(cn=" + userId + "," + connectionParams.getLdapUserBase() + ")(objectclass="
                                + connectionParams.getLdapUserObject() + "))", SearchScope.ONELEVEL, "*");
            while (cursor.next())
            {
                final SearchResultEntry response = (SearchResultEntry) cursor.get();
                final Iterator<EntryAttribute> itEntry = response.getEntry().iterator();
                while (itEntry.hasNext())
                {
                    final EntryAttribute attribute = itEntry.next();
                    final String key = attribute.getId();
                    if ("cn".equalsIgnoreCase(key))
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

        return user;
    }

    @Override
    public void deleteUser(final String userId)
    {
        throw new ActivitiException("LDAP user manager doesn't support deleting a user");
    }

    @Override
    public List<User> findUserByQueryCriteria(final UserQueryImpl query, final Page page)
    {
        LOG.debug("findUserByQueryCriteria");

        final List<User> userList = new ArrayList<User>();

        final StringBuilder searchQuery = new StringBuilder();
        if (StringUtils.isNotEmpty(query.getId()))
        {
            searchQuery.append("(&(cn=")
                .append(query.getId())
                .append(")(objectclass=" + connectionParams.getLdapUserObject() + "))");

        }
        else if (StringUtils.isNotEmpty(query.getLastName()))
        {
            searchQuery.append("(&(sn=")
                .append(query.getLastName())
                .append(")(objectclass=" + connectionParams.getLdapUserObject() + "))");

        }
        else
        {
            searchQuery.append("(&(cn=*)(objectclass=" + connectionParams.getLdapUserObject() + "))");
        }
        LOG.debug("searchQuery: " + searchQuery.toString());

        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapUserBase(),
                searchQuery.toString(), SearchScope.ONELEVEL, "*");
            while (cursor.next())
            {
                final User user = new UserEntity();
                final SearchResultEntry response = (SearchResultEntry) cursor.get();
                final Iterator<EntryAttribute> itEntry = response.getEntry().iterator();
                while (itEntry.hasNext())
                {
                    final EntryAttribute attribute = itEntry.next();
                    final String key = attribute.getId();
                    if ("cn".equalsIgnoreCase(key))
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
                }

                userList.add(user);
            }

            cursor.close();

        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection search failure", e);
        }

        LDAPConnectionUtil.closeConnection(connection);

        return userList;
    }

    @Override
    public long findUserCountByQueryCriteria(final UserQueryImpl query)
    {
        LOG.debug("findUserCountByQueryCriteria");
        return findUserByQueryCriteria(query, null).size();
    }

    @Override
    public Boolean checkPassword(final String userId, final String password)
    {
        LOG.debug("checkPassword");
        boolean credentialsValid = false;
        final LdapConnection connection = new LdapConnection(connectionParams.getLdapServer(),
            connectionParams.getLdapPort());
        try
        {
            LOG.debug("checkPassword: " + "cn=" + userId + "," + connectionParams.getLdapUserBase());
            final BindResponse response = connection.bind(
                "cn=" + userId + "," + connectionParams.getLdapUserBase(), password);
            LOG.debug("result: " + response.getLdapResult().getResultCode());
            if (response.getLdapResult().getResultCode() == ResultCodeEnum.SUCCESS)
            {
                credentialsValid = true;
            }
        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection bind failure", e);

        }

        LDAPConnectionUtil.closeConnection(connection);

        return credentialsValid;
    }
}