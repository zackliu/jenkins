import java.net.*
import groovy.json.JsonSlurper

def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);

def getParameter(variable, specific, template)
{
    if(specific."$variable" != null) return specific."$variable"
    if(template."$variable" != null) return template."$variable"
    return null
}



def createCiJob(jobName, settings, codeBranch) {
    job(jobName) {
        label(getParameter("labelExpression", settings.ci, settings.template))
        logRotator { // Discard old builds
            daysToKeep(getParameter("logRotator.daysToKeep", settings.ci, settings.template)) // If specified, build records are only kept up to this number of days.
            numToKeep(getParameter("logRotator.numToKeep", settings.ci, settings.template)) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(getParameter("logRotator.artifactDaysToKeep", settings.ci, settings.template)) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(getParameter("logRotator.artifactNumToKeep", settings.ci, settings.template)) // If specified, only up to this number of builds have their artifacts retained.
        }
        scm {
            git {
                remote {
                    url(jobSettings['repoUrl'])
                    credentials(jobSettings['credentialId'])
                }
                branch(codeBranch)
            }
        }
        steps {
            shell([
                'npm install',
                'npm run opst init',
                "npm run opst -- deployTheme -B ${codeBranch}"
                ].join('\n'))
        }
        publishers {
            extendedEmail {
                recipientList(jobSettings['mailGroup'])
                defaultSubject('$DEFAULT_SUBJECT')
                defaultContent('$DEFAULT_CONTENT')
                contentType('default')
                triggers {
                    always {
                        sendTo {
                            developers()
                            recipientList()
                        }
                    }
                }
            }
        }
    }
}




def execute(settings) {
    def folderName = settings.folderName
    // create & update a folder for this project
    folder(folderName) {
        displayName(folderName)
    }

    def branches = settings.branches

    branches.each {
        def ciJobName = folderName + '/' + it + '_ci'
        def opbuildJobName = folderName + '/' + it + '_opbuild'
        def e2eJobName = folderName + '/' + it + '_e2e'
        def mergeJobName = folderName + '/' + it + '_merge'
        switch (it) {
            case "develop":
                createCiJob(ciJobName, settings, it)
                createOpbuildJob(opbuildJobName, it + '_ci', settings['template'], settings['e2e'], it)
                createE2eJob(e2eJobName, it + '_opbuild', settings['e2e'], it)
                createMergeJob(mergeJobName, it + '_e2e', settings['template'], it, mergeMapping[it])
                break
            case "release":
                createCiJob(ciJobName, settings['template'], it)
                createOpbuildJob(opbuildJobName, it + '_ci', settings['template'], settings['e2e'], it)
                createE2eJob(e2eJobName, it + '_opbuild', settings['e2e'], it)
                break
            case "hotfix":
                createCiJob(ciJobName, settings['template'], it)
                createOpbuildJob(opbuildJobName, it + '_ci', settings['template'], settings['e2e'], it)
                createE2eJob(e2eJobName, it + '_opbuild', settings['e2e'], it)
                createMergeJob(mergeJobName, it + '_e2e', settings['template'], it, mergeMapping[it])
                break
            case "master":
                createCiJob(ciJobName, settings['template'], it)
                break
        }
    }

    // create & update deliveryPipelineView
    deliveryPipelineView("${folderName}/deliveryPipelineView") {
        pipelineInstances(1)
        columns(1)
        updateInterval(10)
        allowPipelineStart()
        enableManualTriggers()
        allowRebuild()
        configure { view ->
            view / showTestResults(true)
        }
        pipelines {
            component('develop_pipeline', 'develop_ci')
            component('release_pipeline', 'release_ci')
            component('hotfix_pipeline', 'hotfix_ci')
            component('master_pipeline', 'master_ci')
        }
    }
}
