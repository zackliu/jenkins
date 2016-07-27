import java.net.*
import groovy.json.JsonSlurper

def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);

def jsonPayloadJobs = readFileFromWorkspace("git/jobs.json")
def jsonPayloadNode = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/nodeDefault.json").getText()
def jsonPayloadE2e = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/e2eDefault.json").getText()
jobsSettings = new JsonSlurper().parseText(jsonPayloadJobs)
nodeSettings = new JsonSlurper().parseText(jsonPayloadNode)
e2eSettings = new JsonSlurper().parseText(jsonPayloadE2e)

def getParameter(variable, defaultSettings, userSettings)
{
    if(userSettings != null && userSettings["$variable"] != null) return userSettings["$variable"]
    if(defaultSettings != null && defaultSettings["$variable"] != null) return defaultSettings["$variable"]
    return null;
}



def createJob(defaultSettings, userSettings)
{
    def jobName = getParameter("name", defaultSettings, userSettings)
    def folderName = ""
    if(jobName == null) return
    if(jobsSettings.folder != null)
    {
        folderName = jobsSettings.folder
        jobName = folderName + '/' + jobName 
    }
    job(jobName)
    {
        def labelExpression = getParameter("label",defaultSettings, userSettings)
        label(labelExpression)

        def logRotatorEnabled = getParameter("logRotatorEnabled", defaultSettings, userSettings)
        if(logRotatorEnabled == true)
        {
            logRotator 
            { 
                daysToKeep(getParameter("daysToKeep", defaultSettings.logRotator, userSettings.logRotator)) // If specified, build records are only kept up to this number of days.
                numToKeep(getParameter("numToKeep", defaultSettings.logRotator, userSettings.logRotator)) // If specified, only up to this number of build records are kept.
                artifactDaysToKeep(getParameter("artifactDaysToKeep", defaultSettings.logRotator, userSettings.logRotator)) // If specified, artifacts from builds older than this number of days will be deleted, but the logs, history, reports, etc for the build will be kept.
                artifactNumToKeep(getParameter("artifactNumToKeep", defaultSettings.logRotator, userSettings.logRotator)) // If specified, only up to this number of builds have their artifacts retained.
            }
        }

        def gitEnabled = getParameter("gitEnabled",defaultSettings, userSettings)
        if(gitEnabled == true)
        {
            scm
            {
                git
                {
                    remote
                    {
                        name(getParameter("name",defaultSettings.git, userSettings.git))
                        url(getParameter("repo",defaultSettings.git, userSettings.git))
                        credentials(getParameter("credentials",defaultSettings.git, userSettings.git))
                    }
                    branch(getParameter("branches",defaultSettings.git, userSettings.git))
                    extensions
                    {
                        def mergeOptionsEnabled = getParameter("mergeOptionsEnabled",defaultSettings.git, userSettings.git)
                        if(mergeOptionsEnabled == true)
                        {
                            mergeOptions
                            {
                                branch(getParameter("branch",defaultSettings.git.mergeOptions, userSettings.git.mergeOptions))
                                remote(getParameter("remote",defaultSettings.git.mergeOptions, userSettings.git.mergeOptions))
                            }
                        }
                    }
                }
            }
        }

        def triggerEnabled = getParameter("triggerEnabled", defaultSettings, userSettings)
        if(triggerEnabled == true)
        {
            triggers
            {
                upstream(getParameter("upstream", defaultSettings.triggers, userSettings.triggers), "SUCCESS")
            }
        }

        def shellEnabled = getParameter("shellEnabled", defaultSettings, userSettings)
        if(shellEnabled == true)
        {
            steps
            {
                shell(getParameter("shell", defaultSettings, userSettings).join('\n'))
            }
        }

        def publishersEnabled = getParameter("publishersEnabled", defaultSettings, userSettings)
        if(publishersEnabled == true)
        {
            publishers
            {
                if(getParameter("extendedEmailEnabled", defaultSettings.publishers, userSettings.publishers) == true)
                {
                    extendedEmail
                    {
                        recipientList(getParameter("recipientList", defaultSettings.publishers.extendedEmail, userSettings.publishers.extendedEmail))
                        defaultSubject(getParameter("defaultSubject", defaultSettings.publishers.extendedEmail, userSettings.publishers.extendedEmail))
                        defaultContent(getParameter("defaultContent", defaultSettings.publishers.extendedEmail, userSettings.publishers.extendedEmail))
                        contentType(getParameter("contentType", defaultSettings.publishers.extendedEmail, userSettings.publishers.extendedEmail))

                        if(getParameter("triggersAlwaysEnabled", defaultSettings.publishers.extendedEmail, userSettings.publishers.extendedEmail) == true)
                        {
                            triggers
                            {
                                always
                                {
                                    sendTo
                                    {
                                        if(getParameter("developers", defaultSettings.publishers.extendedEmail.triggersAlways, userSettings.publishers.extendedEmail.triggersAlways) == true) developers()
                                        if(getParameter("recipientList", defaultSettings.publishers.extendedEmail.triggersAlways, userSettings.publishers.extendedEmail.triggersAlways) == true) recipientList()
                                    }
                                }
                            }
                        }
                        if(getParameter("triggersFailureEnabled", defaultSettings.publishers.extendedEmail, userSettings.publishers.extendedEmail) == true)
                        {
                            triggers
                            {
                                failure
                                {
                                    sendTo
                                    {
                                        if(getParameter("developers", defaultSettings.publishers.extendedEmail.triggersFailure, userSettings.publishers.extendedEmail.triggersFailure) == true) developers()
                                        if(getParameter("recipientList", defaultSettings.publishers.extendedEmail.triggersFailure, userSettings.publishers.extendedEmail.triggersFailure) == true) recipientList()
                                    }
                                }
                            }
                        }
                        if(getParameter("triggersFixedEnabled", defaultSettings.publishers.extendedEmail, userSettings.publishers.extendedEmail) == true)
                        {
                            triggers
                            {
                                fixed
                                {
                                    sendTo
                                    {
                                        if(getParameter("developers", defaultSettings.publishers.extendedEmail.triggersFixed, userSettings.publishers.extendedEmail.triggersFixed) == true) developers()
                                        if(getParameter("recipientList", defaultSettings.publishers.extendedEmail.triggersFixed, userSettings.publishers.extendedEmail.triggersFixed) == true) recipientList()
                                    }
                                }
                            }
                        }
                    }
                }

                if(getParameter("gitEnabled", defaultSettings.publishers, userSettings.publishers) == true)
                {
                    git
                    {
                        if(getParameter("pushOnlyIfSuccess", defaultSettings.publishers.git, userSettings.publishers.git) == true) pushOnlyIfSuccess()
                        if(getParameter("pushMerge", defaultSettings.publishers.git, userSettings.publishers.git) == true) pushMerge()
                    }
                    
                }
            }
        }
    }
}

def createBranch(userSettings)
{
    branch = userSettings.branch

    userSettings.jobs.each
    {
        jobSettings ->
        if(jobSettings.template == "nodeDefault")
        {

            createJob(nodeSettings, jobSettings)
        }
        else if (jobSettings.template == "e2eDefault")
        {
            createJob(e2eSettings, jobSettings)
        }
        else
        {
            createJob(null, jobSettings)
        }
    }
}

def createWorkflow(userSettings)
{
    if(userSettings.folder != null)
    {
        folder(userSettings.folder)
        {
            displayName(userSettings.folder)
        }
    }

    partner = userSettings.partner
    userSettings.workflow.each
    {
        branchSettings ->
        createBranch(branchSettings)
    }
}

createWorkflow(jobsSettings)
