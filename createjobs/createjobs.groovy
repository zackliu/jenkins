import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import java.net.*
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.envinject.*
import com.cloudbees.hudson.plugins.folder.*


globalNodeProperties = Jenkins.getInstance().getGlobalNodeProperties()
envVarsNodePropertyList = globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)

envVars = envVarsNodePropertyList.get(0).getEnvVars()
MONITORREPO = envVars.find{it.key == "MONITORREPO"}.value

//def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
//clean dir
def commandClean = ["rm", "-rf", "/var/jenkins_home/groovyTemp"]
def proc1 = commandClean.execute()
proc1.waitFor()

//create dir
def commandCreateDir = ["mkdir", "-p", "/var/jenkins_home/groovyTemp"]
def proc2 = commandCreateDir.execute()
proc2.waitFor()

//clone git
def commandCloneGit = ["git", "clone", MONITORREPO, "git"] //if you change repo you need to change this line
def srcDir = new File("/var/jenkins_home/groovyTemp")
def proc3 = commandCloneGit.execute(null, srcDir)
proc3.waitFor()

//get SYSTEM CONFIG JSON
jsonPayload = new File("/var/jenkins_home/groovyTemp/git/config.json").getText() //depends on the structure of your repo
def state = new JsonSlurper().parseText(jsonPayload)

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


def createSeedJobAndRun(name, env, templates, folderName)
{
  def job = null
  //create a new buildersList if the name hasn't been created
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

  //inject ENV
  def envList = []
  env.each{
    def envString = it.key+"="+it.value
    envList.add(envString)
  }
  def envinjectBuilder = new EnvInjectBuilder(null, envList.join('\n'))
  job.buildersList.add(envinjectBuilder)

  //add a shell to clone git to local
  def workspace = "/var/jenkins_home/jobs/$folderName/jobs/$name/workspace"
  def shellBuilder = new hudson.tasks.Shell(["sudo rm -rf $workspace",
                                        "mkdir -p $workspace",
                                        "cd $workspace",
                                        "sudo git clone ${MONITORREPO} ."].join('\n'))
  job.buildersList.add(shellBuilder)

  //add templates
  def builder = new javaposse.jobdsl.plugin.ExecuteDslScripts(
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

  def postBuilder = new hudson.tasks.Shell("sudo rm -rf $workspace")
  job.buildersList.add(postBuilder)

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

folderName = "seedJobs"

if(Jenkins.getInstance().getItemMap().find{it.key == folderName} != null)
{
  folder = Jenkins.getInstance().getItemMap().find{it.key == folderName}.value
}
else
{
  folder = Jenkins.getInstance().createProject(Folder, folderName)
}

state.workflow.each{
  createSeedJobAndRun(it.seedJobName, it.envinject, it.templates, folderName);
}





