
package org.activiti.ldap;

public class LDAPConnectionParams
{
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
}
