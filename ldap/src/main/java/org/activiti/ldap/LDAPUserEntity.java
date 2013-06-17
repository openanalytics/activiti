
package org.activiti.ldap;

import org.activiti.engine.impl.persistence.entity.UserEntity;

public class LDAPUserEntity extends UserEntity
{
    private static final long serialVersionUID = 1L;

    private String commonName;

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(final String commonName)
    {
        this.commonName = commonName;
    }
}
