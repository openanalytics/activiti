
package org.activiti.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.message.BindResponse;
import org.apache.directory.ldap.client.api.message.SearchResponse;
import org.apache.directory.ldap.client.api.message.SearchResultEntry;
import org.apache.directory.shared.ldap.cursor.Cursor;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.filter.SearchScope;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;

public class LDAPUserManager extends AbstractLDAPManager implements UserIdentityManager, Session
{
    public LDAPUserManager(final LDAPConnectionParams params)
    {
        super(params);
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
    public void updateUser(final UserEntity updatedUser)
    {
        throw new ActivitiException("LDAP user manager doesn't support updating a user");
    }

    @Override
    public void deleteUser(final String userId)
    {
        throw new ActivitiException("LDAP user manager doesn't support deleting a user");
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
    public UserQuery createNewUserQuery()
    {
        return new UserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    @Override
    public IdentityInfoEntity findUserInfoByUserIdAndKey(final String userId, final String key)
    {
        throw new ActivitiException(
            "LDAP user manager doesn't support finding user info by user id and key (" + userId + ", " + key
                            + ")");
    }

    @Override
    public List<String> findUserInfoKeysByUserIdAndType(final String userId, final String type)
    {
        throw new ActivitiException(
            "LDAP user manager doesn't support finding user info keys by user id and type (" + userId + ", "
                            + type + ")");
    }

    @Override
    public List<User> findPotentialStarterUsers(final String proceDefId)
    {
        throw new ActivitiException("LDAP user manager doesn't support finding potential starter users ("
                                    + proceDefId + ")");
    }

    @Override
    public List<User> findUsersByNativeQuery(final Map<String, Object> parameterMap,
                                             final int firstResult,
                                             final int maxResults)
    {
        throw new ActivitiException("LDAP group manager doesn't support finding users by native query: "
                                    + parameterMap);
    }

    @Override
    public long findUserCountByNativeQuery(final Map<String, Object> parameterMap)
    {
        return findUsersByNativeQuery(parameterMap, 0, -1).size();
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

        LOGGER.debug("findUserByQueryCriteria: " + searchQuery.toString());

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
        LOGGER.debug("findUserCountByQueryCriteria");
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
            LOGGER.debug("checkPassword: " + userDn);

            final BindResponse response = connection.bind(userDn, password);
            LOGGER.debug("result: " + response.getLdapResult().getResultCode());

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
}
