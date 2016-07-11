import jenkins.model.Jenkins;

pm = Jenkins.instance.pluginManager

uc = Jenkins.instance.updateCenter
updated = false
pm.plugins.each { plugin ->
  if (uc.getPlugin(plugin.shortName).version != plugin.version) {
    update = uc.getPlugin(plugin.shortName).deploy(true)
    update.get()
    updated = true
  }
}
if (updated) {
  Jenkins.instance.restart()
}