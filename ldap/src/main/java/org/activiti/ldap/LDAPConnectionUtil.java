
package org.activiti.ldap;

import org.activiti.engine.ActivitiException;
import org.apache.directory.ldap.client.api.LdapConnection;

public class LDAPConnectionUtil
{
    public static LdapConnection openConnection(final LDAPConnectionParams connectionParams)
    {

        final LdapConnection connection = new LdapConnection(connectionParams.getLdapServer(),
            connectionParams.getLdapPort());
        try
        {
            connection.bind(connectionParams.getLdapUser(), connectionParams.getLdapPassword());
        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection open failure", e);
        }
        return connection;
    }

    public static void closeConnection(final LdapConnection connection)
    {
        try
        {
            connection.unBind();
            connection.close();
        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection close failure", e);
        }
    }
}
