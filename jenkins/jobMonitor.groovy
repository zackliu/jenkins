import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import java.net.*
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.envinject.*
import com.cloudbees.hudson.plugins.folder.*

def jsonPayload = new File("/var/jenkins_home/systemConfig/config.json").getText() //depends on the structure of your repo
state = new JsonSlurper().parseText(jsonPayload);

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

envVars.put("MONITORREPO", "${state.jobMonitor.repo}") //<Key, Value>

instance.save()

def createSeedJobAndRun(name, url, templates, folderName)
{

  if(Jenkins.getInstance().getItem("$folderName").getItem("$name")!= null)
  {
    //println(Jenkins.getInstance().getItem("$folderName").getItem("$name"))
    job = Jenkins.getInstance().getItem("$folderName").getItem("$name")
    job.buildersList.clear()
  }
  else
  {
    job = folder.createProject(FreeStyleProject, name)
    job.displayName = name
  }

  //def text = new URL(templates).getText()
  def text = new File("/var/jenkins_home/systemConfig/jenkins/jobMonitorDsl.groovy").getText()

  env = [
    "REPO=${state.jobMonitor.repo}",
    "CREDENTIALSID=${state.jobMonitor.credentialsId}",
    "BRANCH=${state.jobMonitor.branch}"
    ].join('\n');

  envinjectBuilder = new EnvInjectBuilder(null, env)
  job.buildersList.add(envinjectBuilder)


  builder = new javaposse.jobdsl.plugin.ExecuteDslScripts(
    new javaposse.jobdsl.plugin.ExecuteDslScripts.ScriptLocation(
        'true',
        null,
        text,
    ),
    false,
    javaposse.jobdsl.plugin.RemovedJobAction.DELETE, 
    javaposse.jobdsl.plugin.RemovedViewAction.DELETE, 
    javaposse.jobdsl.plugin.LookupStrategy.JENKINS_ROOT
  )
  job.buildersList.add(builder)

  job.save()

  job.getAllJobs().each{
    run ->
    if(run.getDisplayName() == name){
      run.scheduleBuild();
    // run.delete(); 
    }
  }
}

if(Jenkins.getInstance().getItemMap().find{it.key == "jobMonitor"} != null)
{
  folder = Jenkins.getInstance().getItemMap().find{it.key == "jobMonitor"}.value
}
else
{
  folder = Jenkins.getInstance().createProject(Folder, "jobMonitor")
}

createSeedJobAndRun("jobMonitor_Seed", null, null, "jobMonitor");



