import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import java.net.*
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.envinject.*
import com.cloudbees.hudson.plugins.folder.*

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



def createSeedJobAndRun(name, url, templates, folderName)
{

  if(Jenkins.getInstance().getItemMap().find{it.key == "$folderName/$name"} != null)
  {
  //  Jenkins.getInstance().remove(Jenkins.getInstance().getItemMap().find{it.key == name}.value)
    job = Jenkins.getInstance().getItemMap().find{it.key == "$folderName/$name"}.value
    job.buildersList.clear()
  }
  else
  {
    job = folder.createProject(FreeStyleProject, name)
    job.displayName = name
  }

  def text = new URL(templates).getText()

  url = "TURL="+url
  envinjectBuilder = new EnvInjectBuilder(null, url)
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

if(Jenkins.getInstance().getItemMap().find{it.key == "jobMoniter"} != null)
{
  folder = Jenkins.getInstance().getItemMap().find{it.key == "jobMoniter"}.value
}
else
{
  folder = Jenkins.getInstance().createProject(Folder, "jobMoniter")
}

createSeedJobAndRun("jobMonitor_Seed", null, state.jobMonitor, "jobMoniter");



