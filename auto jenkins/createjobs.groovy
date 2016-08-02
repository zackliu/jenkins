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


def createSeedJobAndRun(name, env, templates)
{
  //create a new buildersList if the name hasn't been created
  if(Jenkins.getInstance().getItemMap().find{it.key == name} != null)
  {
    job = Jenkins.getInstance().getItemMap().find{it.key == name}.value
    job.buildersList.clear()
  }
  else
  {
    job = Jenkins.instance.createProject(FreeStyleProject, name)
    job.displayName = name
  }

  //inject ENV
  def envList = []
  env.each{
    envString = it.key+"="+it.value
    envList.add(envString)
  }
  envinjectBuilder = new EnvInjectBuilder(null, envList.join('\n'))
  job.buildersList.add(envinjectBuilder)

  //add a shell to clone git to local
  shellBuilder = new hudson.tasks.Shell(["rm -rf /var/jenkins_home/jobs/seed_job/workspace",
                                        "mkdir -p /var/jenkins_home/jobs/seed_job/workspace",
                                        "cd /var/jenkins_home/jobs/seed_job/workspace",
                                        "git clone https://github.com/zackliu/jenkins.git ."].join('\n'))
  job.buildersList.add(shellBuilder)

  //add templates
  builder = new javaposse.jobdsl.plugin.ExecuteDslScripts(
    new javaposse.jobdsl.plugin.ExecuteDslScripts.ScriptLocation(
        "false",
        templates.join('\n'),
        null
    ),
    false,
    javaposse.jobdsl.plugin.RemovedJobAction.DELETE, 
    javaposse.jobdsl.plugin.RemovedViewAction.DELETE, 
    javaposse.jobdsl.plugin.LookupStrategy.JENKINS_ROOT
  )
  job.buildersList.add(builder)

  //save all builds
  job.save()

  //run seed job
  job.getAllJobs().each{
    run ->
    if(run.getDisplayName() == name){
      run.scheduleBuild();
    // run.delete(); 
    }
  }
}

state.workflow.each{
  createSeedJobAndRun(it.seedJobName, it.envinject, it.templates);
}





