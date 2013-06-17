
package org.activiti.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

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
        LDAPUserEntity user = null;
        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);

        try
        {
            final String filter = "(&(" + connectionParams.getLdapUserIdAttribute() + "=" + userId
                                  + ")(objectclass=" + connectionParams.getLdapUserObject() + "))";

            LOG.debug("findUserById: " + filter);

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

    @Override
    public void deleteUser(final String userId)
    {
        throw new ActivitiException("LDAP user manager doesn't support deleting a user");
    }

    @Override
    public List<User> findUserByQueryCriteria(final UserQueryImpl query, final Page page)
    {
        final List<User> userList = new ArrayList<User>();

        final StringBuilder searchQuery = new StringBuilder("(&");
        if (StringUtils.isNotEmpty(query.getId()))
        {
            searchQuery.append("(" + connectionParams.getLdapUserIdAttribute() + "=")
                .append(query.getId())
                .append(")");
        }
        else if (StringUtils.isNotEmpty(query.getLastName()))
        {
            searchQuery.append("(sn=").append(query.getLastName()).append(")");
        }
        else
        {
            searchQuery.append("(" + connectionParams.getLdapUserIdAttribute() + "=*)");
        }
        searchQuery.append("(objectclass=").append(connectionParams.getLdapUserObject()).append("))");

        LOG.debug("findUserByQueryCriteria: " + searchQuery.toString());

        final LdapConnection connection = LDAPConnectionUtil.openConnection(connectionParams);
        try
        {
            final Cursor<SearchResponse> cursor = connection.search(connectionParams.getLdapUserBase(),
                searchQuery.toString(), SearchScope.ONELEVEL, "*");
            while (cursor.next())
            {
                final LDAPUserEntity user = new LDAPUserEntity();
                final SearchResultEntry response = (SearchResultEntry) cursor.get();
                final Iterator<EntryAttribute> itEntry = response.getEntry().iterator();

                while (itEntry.hasNext())
                {
                    final EntryAttribute attribute = itEntry.next();
                    setUserAttribute(attribute, user);
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
        final String userDn = getUserDn(userId);

        if (StringUtils.isBlank(userDn))
        {
            return false;
        }

        final LdapConnection connection = new LdapConnection(connectionParams.getLdapServer(),
            connectionParams.getLdapPort());

        try
        {
            LOG.debug("checkPassword: " + userDn);

            final BindResponse response = connection.bind(userDn, password);
            LOG.debug("result: " + response.getLdapResult().getResultCode());

            return (response.getLdapResult().getResultCode() == ResultCodeEnum.SUCCESS);
        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection bind failure", e);
        }
        finally
        {
            LDAPConnectionUtil.closeConnection(connection);
        }
    }

    private String getUserDn(final String userId)
    {
        if ("cn".equalsIgnoreCase(connectionParams.getLdapUserIdAttribute()))
        {
            return "cn=" + userId + "," + connectionParams.getLdapUserBase();
        }
        else
        {
            final LDAPUserEntity user = (LDAPUserEntity) findUserById(userId);

            if (user == null)
            {
                return null;
            }

            return "cn=" + user.getCommonName() + "," + connectionParams.getLdapUserBase();
        }
    }

    private void setUserAttribute(final EntryAttribute attribute, final LDAPUserEntity user)
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
}
