import java.net.*
import groovy.json.JsonSlurper

def jsonPayloadJobs = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/jobs.json").getText()
def jsonPayloadNode = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/nodeDefault.json").getText()
def jsonPayloadE2e = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/e2eDefault.json").getText()
def jobsSettings = new JsonSlurper().parseText(jsonPayloadJobs)
def nodeSettings = new JsonSlurper().parseText(jsonPayloadNode)
def e2eSettings = new JsonSlurper().parseText(jsonPayloadE2e)

def getParameter(variable, defaultSettings, userSettings)
{
    if(userSettings["$variable"] != null) return userSettings["$variable"]
    if(defaultSettings["$variable"] != null) return defaultSettings["$variable"]
    return null;
}



def createJob(defaultSettings, userSettings)
{
    def jobName = getParameter("name", defaultSettings, userSettings)
    if(jobName == null) return
    job(jobName)
    {
        
    }
}

createJob(jobsSettings.workflow[0].jobs[0], nodeSettings)