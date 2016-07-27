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
        shell("sudo node createjobs.js")
    }
}
