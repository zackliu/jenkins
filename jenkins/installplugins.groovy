import jenkins.model.Jenkins
import hudson.security.*
import java.net.*
import groovy.json.JsonSlurper

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

pm = Jenkins.instance.pluginManager
uc = Jenkins.instance.updateCenter
def jsonPayload = new File("/var/jenkins_home/systemConfig/config.json").getText() //depends on the structure of your repo
def state = new JsonSlurper().parseText(jsonPayload);



deployed = false
def activatePlugin(plugin) {
  if (! plugin.isEnabled()) {
    plugin.enable()
    deployed = true
  }

  plugin.getDependencies().each {
    activatePlugin(pm.getPlugin(it.shortName))
  }
}

state.plugins.each {
  if (! pm.getPlugin(it)) {
    deployment = uc.getPlugin(it).deploy(true)
    deployment.get()
  }
  activatePlugin(pm.getPlugin(it))
}

updated = false
pm.plugins.each { plugin ->
  if (uc.getPlugin(plugin.shortName).version != plugin.version) {
    update = uc.getPlugin(plugin.shortName).deploy(true)
    update.get()
    updated = true
  }
}

jenkins.model.Jenkins.getInstance().restart()


