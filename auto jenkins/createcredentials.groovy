import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*
import groovy.json.JsonSlurper

def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);


checkedTimes = 3

def checkStarted()
{
  if(Jenkins == null)
  {
    checkedTimes = checkedTimes - 1
    if(checkedTimes <= 0) exit(1)
    sleep(5000)
    checkStarted()
  }
  return
}

checkStarted()


//create credentials
domain = Domain.global()
store = Jenkins.getInstance().getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

state.credentials.each
{
  usernameAndPassword = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    it.id, //ID
    it.description, //Description
    it.username, //Username
    it.password //Password
  )

  store.addCredentials(domain, usernameAndPassword) //return true or false
}


/*

usernameAndPassword = new UsernamePasswordCredentialsImpl(
  CredentialsScope.GLOBAL,
  "jenkins-slave-password", //ID
  "Jenkis Slave with Password Configuration", //Description
  "jenkins", //Username
  "jenkins" //Password
)

store.addCredentials(domain, usernameAndPassword) //return true or false

usernameAndPassword = new UsernamePasswordCredentialsImpl(
  CredentialsScope.GLOBAL,
  "zackliu-github", //ID
  "Github with Password Configuration", //Description
  "jenkins", //Username
  "jenkins" //Password
)

store.addCredentials(domain, usernameAndPassword) 
/*
//print credentials
creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
         com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
         Jenkins.instance,
         null,
         null
        )

creds.each{println it.username +" "+ it.id+" "+ it.description+" "+ it.scope}

//set Environment Variables
instance = Jenkins.getInstance()
globalNodeProperties = instance.getGlobalNodeProperties()
envVarsNodePropertyList = globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)

if ( envVarsNodePropertyList == null || envVarsNodePropertyList.size() == 0 ) {
  newEnvVarsNodeProperty = new hudson.slaves.EnvironmentVariablesNodeProperty();
  globalNodeProperties.add(newEnvVarsNodeProperty)
  envVars = newEnvVarsNodeProperty.getEnvVars()
} else {
  envVars = envVarsNodePropertyList.get(0).getEnvVars()
}

envVars.put("FOO", "foo") //<Key, Value>

instance.save()
*/