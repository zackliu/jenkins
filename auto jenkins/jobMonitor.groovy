import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import java.net.*
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.envinject.*

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



def createSeedJobAndRun(name, url, templates)
{

  if(Jenkins.getInstance().getItemMap().find{it.key == name} != null)
  {
  //  Jenkins.getInstance().remove(Jenkins.getInstance().getItemMap().find{it.key == name}.value)
    job = Jenkins.getInstance().getItemMap().find{it.key == name}.value
    job.buildersList.clear()
  }
  else
  {
    job = Jenkins.instance.createProject(FreeStyleProject, name)
    job.displayName = name
  }

  def text = new URL(templates).getText()

  url = "TURL="+url
  envinjectBuilder = new EnvInjectBuilder(null, url)
  job.buildersList.add(envinjectBuilder)

  shellBuilder = new hudson.tasks.Shell(["rm -rf /var/jenkins_home/jobs/seed_job/workspace/git",
                                        "mkdir -p /var/jenkins_home/jobs/seed_job/workspace/git",
                                        "cd /var/jenkins_home/jobs/seed_job/workspace/git",
                                        "git clone https://github.com/zackliu/jenkins.git ."].join('\n'))

  job.buildersList.add(shellBuilder)

  builder = new javaposse.jobdsl.plugin.ExecuteDslScripts(
    new javaposse.jobdsl.plugin.ExecuteDslScripts.ScriptLocation(
        null,
        "git/dslJob.groovy",
        null
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

createSeedJobAndRun("jobMonitor_Seed", it.url, state.jobMonitor);



