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
    def labelExpression = getParameter("labelExpression", settings.ci, settings.template)
    def repoUrl = getParameter("repoUrl", settings.ci, settings.template)
    def credentialId = getParameter("credentialId", settings.ci, settings.template)
    def mailGroup = getParameter("mailGroup", settings.ci, settings.template)
    job(jobName) {

        label(labelExpression)
        logRotator { // Discard old builds
            daysToKeep(settings.template.logRotator.daysToKeep) // If specified, build records are only kept up to this number of days.
            numToKeep(settings.template.logRotator.numToKeep) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(settings.template.logRotator.artifactDaysToKeep) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(settings.template.logRotator.artifactNumToKeep) // If specified, only up to this number of builds have their artifacts retained.
        }

        scm {
            git {
                remote {
                    url(repoUrl)
                    credentials(credentialId)
                }
                branch(codeBranch)
            }
        }

        steps {
            shell(settings.ci.shell.join('\n'))
        }

        publishers {
            extendedEmail {
                recipientList(mailGroup)
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

def createE2eJob(jobName, upstreamJobName, settings, testBranch) {
    def neast = testBranch + "_e2e"
    def e2eSettings = settings.e2e."$neast"
    def labelExpression = getParameter("labelExpression", settings.e2e, settings.template)
    def repoUrl = getParameter("repoUrl", settings.e2e, settings.template)
    def credentialId = getParameter("credentialId", settings.e2e, settings.template)
    def mailGroup = getParameter("mailGroup", settings.e2e, settings.template)

    job(jobName) {

        label(labelExpression)

        logRotator { // Discard old builds
            daysToKeep(settings.template.logRotator.daysToKeep) // If specified, build records are only kept up to this number of days.
            numToKeep(settings.template.logRotator.numToKeep) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(settings.template.logRotator.artifactDaysToKeep) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(settings.template.logRotator.artifactNumToKeep) // If specified, only up to this number of builds have their artifacts retained.
        }

        triggers {
            upstream(upstreamJobName, 'SUCCESS')
        }

        scm {
            git {
                remote {
                    url(repoUrl)
                    credentials(credentialId)
                }
                branch(e2eSettings.sourceBranch)
            }
        }

        steps {
            shell(settings.e2e.shell.join('\n'))
        }

        publishers {
            artifactArchiver {
                artifacts('result/e2e/screenshots/**/*.*') // Archive E2E test results
            }
            postBuildScripts {
                onlyIfBuildSucceeds(false)
                steps {
                    batchFile('START taskkill /f /im chrome.exe \n' +
                              'START taskkill /f /im chromedriver.exe')
                }
            }
            extendedEmail {
                recipientList(mailGroup)
                defaultSubject('$DEFAULT_SUBJECT')
                defaultContent('$DEFAULT_CONTENT')
                contentType('default')
                triggers {
                    failure {
                        sendTo {
                            developers()
                            recipientList()
                        }
                    }
                    fixed {
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


def createMergeJob(jobName, upstreamJobName, settings, codeBranch, mergeTo) {
    def labelExpression = getParameter("labelExpression", settings.merge, settings.template)
    def repoUrl = getParameter("repoUrl", settings.merge, settings.template)
    def parts = repoUrl.split(/[\/\\]/)
    def repoName = parts[-1]
    def credentialId = getParameter("credentialId", settings.merge, settings.template)
    def mailGroup = getParameter("mailGroup", settings.merge, settings.template)

    job(jobName) {
        label(labelExpression)
        logRotator { // Discard old builds
            daysToKeep(settings.template.logRotator.daysToKeep) // If specified, build records are only kept up to this number of days.
            numToKeep(settings.template.logRotator.numToKeep) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(settings.template.logRotator.artifactDaysToKeep) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(settings.template.logRotator.artifactNumToKeep) // If specified, only up to this number of builds have their artifacts retained.
        }
        triggers {
            upstream(upstreamJobName, 'SUCCESS')
        }
        scm {
            git {
                remote {
                    name(repoName)
                    url(repoUrl)
                    credentials(credentialId)
                }
                branch(codeBranch)
                extensions {
                    mergeOptions {
                        branch(mergeTo) // Sets the name of the branch to merge.
                        remote(repoName) // Sets the name of the repository that contains the branch to merge.
                    }
                }
            }
        }
        publishers {
            git {
                pushOnlyIfSuccess()
                pushMerge()
            }
        }
    }
}


def createOpbuildJob(jobName, upstreamJobName, templateJobSettings, e2eJobSettings, testBranch) {
  
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
