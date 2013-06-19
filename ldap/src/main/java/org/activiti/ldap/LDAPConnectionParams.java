
package org.activiti.ldap;

import java.util.Collections;
import java.util.Set;

public class LDAPConnectionParams
{
    public static final String ACTIVITI_SECURITY_ROLE = "security-role";
    public static final String ACTIVITI_SECURITY_ROLE_USER = "user";
    public static final String ACTIVITI_SECURITY_ROLE_ADMIN = "admin";

    private String ldapServer;
    private int ldapPort;

    private String ldapUser;
    private String ldapPassword;

    private String ldapGroupBase;
    private String ldapUserBase;

    private String ldapGroupObject;
    private String ldapUserObject;

    private String ldapUserIdAttribute = "cn";
    private String ldapGroupMemberAttribute = "member";

    private Set<String> activitiUserGroupCns = Collections.singleton(ACTIVITI_SECURITY_ROLE_USER);
    private Set<String> activitiAdminGroupCns = Collections.singleton(ACTIVITI_SECURITY_ROLE_ADMIN);

    public boolean isCommonNameUserId()
    {
        return "cn".equalsIgnoreCase(ldapUserIdAttribute);
    }

    public String getLdapServer()
    {
        return ldapServer;
    }

    public void setLdapServer(final String ldapServer)
    {
        this.ldapServer = ldapServer;
    }

    public int getLdapPort()
    {
        return ldapPort;
    }

    public void setLdapPort(final int ldapPort)
    {
        this.ldapPort = ldapPort;
    }

    public String getLdapUser()
    {
        return ldapUser;
    }

    public void setLdapUser(final String ldapUser)
    {
        this.ldapUser = ldapUser;
    }

    public String getLdapPassword()
    {
        return ldapPassword;
    }

    public void setLdapPassword(final String ldapPassword)
    {
        this.ldapPassword = ldapPassword;
    }

    public String getLdapGroupBase()
    {
        return ldapGroupBase;
    }

    public void setLdapGroupBase(final String ldapGroupBase)
    {
        this.ldapGroupBase = ldapGroupBase;
    }

    public String getLdapUserBase()
    {
        return ldapUserBase;
    }

    public void setLdapUserBase(final String ldapUserBase)
    {
        this.ldapUserBase = ldapUserBase;
    }

    public String getLdapGroupObject()
    {
        return ldapGroupObject;
    }

    public void setLdapGroupObject(final String ldapGroupObject)
    {
        this.ldapGroupObject = ldapGroupObject;
    }

    public String getLdapUserObject()
    {
        return ldapUserObject;
    }

    public void setLdapUserObject(final String ldapUserObject)
    {
        this.ldapUserObject = ldapUserObject;
    }

    public String getLdapUserIdAttribute()
    {
        return ldapUserIdAttribute;
    }

    public void setLdapUserIdAttribute(final String ldapUserIdAttribute)
    {
        this.ldapUserIdAttribute = ldapUserIdAttribute;
    }

    public void setLdapGroupMemberAttribute(final String ldapGroupMemberAttribute)
    {
        this.ldapGroupMemberAttribute = ldapGroupMemberAttribute;
    }

    public String getLdapGroupMemberAttribute()
    {
        return ldapGroupMemberAttribute;
    }

    public Set<String> getActivitiAdminGroupCns()
    {
        return activitiAdminGroupCns;
    }

    public void setActivitiAdminGroupCns(final Set<String> activitiAdminGroupCns)
    {
        this.activitiAdminGroupCns = activitiAdminGroupCns;
    }

    public Set<String> getActivitiUserGroupCns()
    {
        return activitiUserGroupCns;
    }

    public void setActivitiUserGroupCns(final Set<String> activitiUserGroupCns)
    {
        this.activitiUserGroupCns = activitiUserGroupCns;
    }
}
