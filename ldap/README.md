Activiti code
==============

LDAP module for Activiti works with AD, OpenLDAP etc with support for the Manage Tab of the explorer application  

- LDAP groups with admin and user as cn have security-role as type
- rest of the group are automatically assignmed as type

Change the following in the activiti-explorer application, using values relevant to your case:

activiti-standalone-context.xml 
-----------------------------------

    <bean id="processEngineConfiguration" class="org.activiti.spring.SpringProcessEngineConfiguration">
      ...

      <property name="customSessionFactories">
        <list>
          <bean class="org.activiti.ldap.LDAPUserManagerFactory">
            <constructor-arg ref="ldapConnectionParams" />
          </bean>
          <bean class="org.activiti.ldap.LDAPGroupManagerFactory">
            <constructor-arg ref="ldapConnectionParams" />
          </bean>
        </list>
      </property>
    </bean>

    <bean id="ldapConnectionParams"   class="org.activiti.ldap.LDAPConnectionParams">
       <property name="ldapServer"      value="192.168.80.159" />
       <property name="ldapPort"        value="389" />
       <property name="ldapUser"        value="CN=Administrator,CN=Users,DC=alfa,DC=local" />
       <property name="ldapPassword"    value="Welcome05" />
       <property name="ldapUserBase"    value="CN=Users,DC=alfa,DC=local" />
       <property name="ldapGroupBase"   value="CN=Users,DC=alfa,DC=local" />     
       <property name="ldapUserObject"  value="user" />
       <property name="ldapGroupObject" value="group" />     
    </bean>
