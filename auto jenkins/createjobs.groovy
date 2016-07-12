import jenkins.model.Jenkins;
import hudson.model.FreeStyleProject;
import java.net.*
import groovy.json.JsonSlurper

def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);


/*oldSeedJob = Jenkins.instance.getItem("seed-job")
if(oldSeedJob != null){
  oldSeedJob.delete();
}
*/

def createSeedJobAndRun(def name, def url)
{
  job = Jenkins.instance.createProject(FreeStyleProject, name)
  job.displayName = name

  def text = new URL(url).getText()


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

state.seedJob.each{
  createSeedJobAndRun(it.name, it.dslTemplateUrl);
}





