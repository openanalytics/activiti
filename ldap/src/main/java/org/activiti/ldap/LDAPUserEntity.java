package org.activiti.ldap;

import org.activiti.engine.impl.persistence.entity.UserEntity;

public class LDAPUserEntity extends UserEntity
{
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