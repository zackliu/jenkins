import jenkins.model.Jenkins
import hudson.security.*
import java.net.*
import groovy.json.JsonSlurper

checkedTimes = 100

def checkStarted()
{
  if(Jenkins == null || Jenkins.getInstance() == null)
  {
    checkedTimes = checkedTimes - 1
    if(checkedTimes <= 0) exit(1)
    sleep(5000)
    checkStarted()
  }
  return
}
checkStarted()

def jsonPayload = new File("/var/jenkins_home/systemConfig/config.json").getText() //depends on the structure of your repo
def state = new JsonSlurper().parseText(jsonPayload);

def deployed = false
def activatePlugin(plugin) {
  if (! plugin.isEnabled()) {
    plugin.enable()
    deployed = true
  }

  plugin.getDependencies().each {
    activatePlugin(Jenkins.getInstance().pluginManager.getPlugin(it.shortName))
  }
}

state.plugins.each {
  if (! Jenkins.getInstance().pluginManager.getPlugin(it)) {
    while(Jenkins.getInstance().updateCenter == null || Jenkins.getInstance().updateCenter.getPlugin(it) == null)
    {
      sleep(5000)
    }
    deployment = Jenkins.getInstance().updateCenter.getPlugin(it).deploy(true)
    deployment.get()
  }
  activatePlugin(Jenkins.getInstance().pluginManager.getPlugin(it))
}

updated = false
Jenkins.getInstance().pluginManager.plugins.each { plugin ->
  if (Jenkins.getInstance().updateCenter.getPlugin(plugin.shortName).version != plugin.version) {
    update = Jenkins.getInstance().updateCenter.getPlugin(plugin.shortName).deploy(true)
    update.get()
    updated = true
  }
}

jenkins.model.Jenkins.getInstance().restart()


