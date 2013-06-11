
package org.activiti.ldap;

import org.activiti.engine.ActivitiException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.ldap.client.api.LdapConnection;

public class LDAPConnectionUtil
{
    private static final Log LOG = LogFactory.getLog(LDAPConnectionUtil.class);

    public static LdapConnection openConnection(final LDAPConnectionParams connectionParams)
    {
        try
        {
            final LdapConnection connection = new LdapConnection(connectionParams.getLdapServer(),
                connectionParams.getLdapPort());

            if (StringUtils.isNotBlank(connectionParams.getLdapUser()))
            {
                connection.bind(connectionParams.getLdapUser(), connectionParams.getLdapPassword());
            }
            else
            {
                connection.bind();
            }

            return connection;
        }
        catch (final Exception e)
        {
            throw new ActivitiException("LDAP connection open failure", e);
        }
    }

    public static void closeConnection(final LdapConnection connection)
    {
        try
        {
            connection.unBind();
        }
        catch (final Exception e)
        {
            LOG.error("Failed to unbind: " + connection, e);
        }

        try
        {
            connection.close();
        }
        catch (final Exception e)
        {
            LOG.error("Failed to close: " + connection, e);
        }
    }
}
