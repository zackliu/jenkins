job("jobMonitor")
{
    label("createJob_slave")
    scm
    {
        git
        {
            remote
            {
                url("https://github.com/zackliu/jenkins")
                credentials("zackliu-github")
            }
            branch("master")
        }
    }
    
    triggers
    {
        scm('')
    }

    steps
    {
        shell("cd /home/jenkins/createjobs && sudo node createjobs.js")
    }
}
