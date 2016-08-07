import java.net.*
import groovy.json.JsonSlurper

def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);

def jsonPayloadJobs = readFileFromWorkspace(FILE)
jobsSettings = new JsonSlurper().parseText(jsonPayloadJobs)

/*
 ***************************************************************************************
 * Below is the groovy script to create/update Jenkins job.
 * Pls reference to https://jenkinsci.github.io/job-dsl-plugin/ for supported syntax by Jenkins DSL plugin.
 ***************************************************************************************
 */
def createJob(jobName, upstreamJobName, jobSettings, codeBranch, buildSteps) {
    job(jobName) {
        label(jobSettings['labelExpression'])
        logRotator { // Discard old builds
            daysToKeep(14) // If specified, build records are only kept up to this number of days.
            numToKeep(40) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(14) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(40) // If specified, only up to this number of builds have their artifacts retained.
        }
        if (upstreamJobName) {
            triggers {
                upstream(upstreamJobName, 'SUCCESS')
            }
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
            shell(buildSteps.join('\n'))
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

def createOpbuildJob(jobName, upstreamJobName, jobSettings, testBranch, githubToken) {
    def contentBranch = jobSettings['branchMapping'][testBranch]['contentBranch']
    def opbuildEndpoint = jobSettings['branchMapping'][testBranch]['opbuildEnv']
    job(jobName) {
        label(jobSettings['labelExpression'])
        logRotator { // Discard old builds
            daysToKeep(14) // If specified, build records are only kept up to this number of days.
            numToKeep(40) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(14) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(40) // If specified, only up to this number of builds have their artifacts retained.
        }
        triggers {
            upstream(upstreamJobName, 'SUCCESS')
        }
        scm {
            git {
                remote {
                    url(jobSettings['repoUrl'])
                    credentials(jobSettings['credentialId'])
                }
                branch('develop')
            }
        }
        steps {
            shell([
                'npm install \n',
                "gulp merge --repo openpublishing-test --gitUser Microsoft --gitToken ${githubToken} --sourceBranch master --targetBranch ${contentBranch}",
                "gulp triggerBuild --buildEndpoint ${opbuildEndpoint} --buildBranch ${contentBranch}"
                ].join('\n'))
        }
        publishers {
            extendedEmail {
                recipientList(jobSettings['mailGroup'])
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

def createE2eJob(jobName, upstreamJobName, jobSettings, testBranch) {
    def e2eSettings = jobSettings['branchMapping'][testBranch]
    job(jobName) {
        label(jobSettings['labelExpression'])
        logRotator { // Discard old builds
            daysToKeep(14) // If specified, build records are only kept up to this number of days.
            numToKeep(40) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(14) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(40) // If specified, only up to this number of builds have their artifacts retained.
        }
        triggers {
            upstream(upstreamJobName, 'SUCCESS')
        }
        scm {
            git {
                remote {
                    url(jobSettings['repoUrl'])
                    credentials(jobSettings['credentialId'])
                }
                branch(e2eSettings['sourceBranch'])
            }
        }
        steps {
            shell([
                '#!/bin/bash',
                'export DISPLAY=:0',
                'sudo Xvfb :0 -ac -screen 0 1920x1080x24 &',
                'webdriver-manager start &',
                'npm install',
                'npm update',
                'sed -i \'s/maxInstances: 6/maxInstances: 2/g\' test/Docs/protractor.config.js',
                'gulp clean'
                "gulp e2e --configFile ${e2eSettings['configFile']} --baseUrl ${e2eSettings['baseUrl']} --branchName ${e2eSettings['contentBranch']}"
                ].join('\n'))
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
                recipientList(jobSettings['mailGroup'])
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

def createMergeJob(jobName, upstreamJobName, jobSettings, codeBranch, mergeTo) {
    def parts = jobSettings['repoUrl'].split(/[\/\\]/)
    def repoName = parts[-1]
    job(jobName) {
        label(jobSettings['labelExpression'])
        logRotator { // Discard old builds
            daysToKeep(14) // If specified, build records are only kept up to this number of days.
            numToKeep(40) // If specified, only up to this number of build records are kept.
            artifactDaysToKeep(14) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
            artifactNumToKeep(40) // If specified, only up to this number of builds have their artifacts retained.
        }
        triggers {
            upstream(upstreamJobName, 'SUCCESS')
        }
        scm {
            git {
                remote {
                    name(repoName)
                    url(jobSettings['repoUrl'])
                    credentials(jobSettings['credentialId'])
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

def decode(text) {
    return new String(text.decodeBase64())
}

def execute(settings) {
    def folderName = "ops_${settings['partnerName']}_template"
    // create & update a folder for this project
    folder(folderName) {
        displayName(folderName)
    }

    def branches = ['develop', 'release', 'hotfix', 'master']
    def mergeMapping = [
        develop: 'release',
        release: 'master',
        hotfix: 'master'
    ]
    def bumpMapping = [
        develop: 'prerelease',
        release: 'minor',
        hotfix: 'patch'
    ]
    def gitToken = decode("adfasd")
    branches.each {
        def ciJobName = folderName + '/' + it + '_ci'
        def opbuildJobName = folderName + '/' + it + '_opbuild'
        def e2eJobName = folderName + '/' + it + '_e2e'
        def mergeJobName = folderName + '/' + it + '_merge'
        def versionJobName = folderName + '/' + it + '_bump_version'
        def locciJobName = folderName + '/' + it + '_locci'

        def buildSteps = [
            'npm install',
            'npm update',
            'npm run opst init',
            "npm run opst -- deployTheme -B docker_${it}"
        ]
        def locBuildSteps = [
            'npm install',
            'npm update',
            'npm run opst init',
            "npm run opst -- deployLocTheme -B ${it}"
        ]
        def bumpVersionSteps = [
            'npm version ' + bumpMapping[it],
            'git push --follow-tags ' + settings['ci']['repoUrl'] + ' HEAD:' + it
        ]
        switch (it) {
            case "develop":
                createJob(ciJobName, null, settings['ci'], it, buildSteps)
                createOpbuildJob(opbuildJobName, it + '_ci', settings['opbuild'], it, gitToken)
                createE2eJob(e2eJobName, it + '_opbuild', settings['e2e'], it)
                createMergeJob(mergeJobName, it + '_e2e', settings['ci'], it, mergeMapping[it])
                break
            case "release":
                createJob(ciJobName, null, settings['ci'], it, buildSteps)
                createOpbuildJob(opbuildJobName, it + '_ci', settings['opbuild'], it, gitToken)
                createE2eJob(e2eJobName, it + '_opbuild', settings['e2e'], it)
                createJob(versionJobName, null, settings['ci'], it, bumpVersionSteps)
                break
            case "hotfix":
                createJob(ciJobName, null, settings['ci'], it, buildSteps)
                createOpbuildJob(opbuildJobName, it + '_ci', settings['opbuild'], it, gitToken)
                createE2eJob(e2eJobName, it + '_opbuild', settings['e2e'], it)
                createJob(versionJobName, null, settings['ci'], it, bumpVersionSteps)
                break
            case "master":
                createJob(ciJobName, null, settings['ci'], it, buildSteps)
                createJob(locciJobName, it + '_ci', settings['ci'], it, locBuildSteps)
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
        showTestResults(true)
        pipelines {
            component('develop_pipeline', 'develop_ci')
            component('release_pipeline', 'release_ci')
            component('hotfix_pipeline', 'hotfix_ci')
            component('master_pipeline', 'master_ci')
            component('Bump hotfix branch package version and add git tag', 'hotfix_bump_version')
            component('Bump release branch package version and add git tag', 'release_bump_version')
        }
    }
}

// Main entry point
execute(jobsSettings)
