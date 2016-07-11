import jenkins.model.Jenkins;
import hudson.model.FreeStyleProject;
import java.net.*

oldSeedJob = Jenkins.instance.getItem("seed-job")
if(oldSeedJob != null){
  oldSeedJob.delete();
}


job = Jenkins.instance.createProject(FreeStyleProject, 'seed-job')
job.displayName = 'Seed Job'

def text = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/dslJob.groovy").getText()


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
  if(run.getDisplayName() == 'Seed Job'){
    run.scheduleBuild();
   // run.delete(); 
  }
}



