import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import java.net.*
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.envinject.*

def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);


/*oldSeedJob = Jenkins.instance.getItem("seed-job")
if(oldSeedJob != null){
  oldSeedJob.delete();
}
*/

def createSeedJobAndRun(def name, def url)
{

  if(Jenkins.getInstance().getItemMap().find{it.key == name} != null)
  {
    Jenkins.getInstance().remove(Jenkins.getInstance().getItemMap().find{it.key == name}.value)
  }

  job = Jenkins.instance.createProject(FreeStyleProject, name)
  job.displayName = name

  def text = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/dslJob.groovy").getText()

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

state.workflow.each{
  createSeedJobAndRun(it.seedJobName, it.url);
}





