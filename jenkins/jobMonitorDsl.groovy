job("jobMonitor/jobMonitor")
{
    label("createJob_slave")
    scm
    {
        git
        {
            remote
            {
                url(REPO)
                credentials(CREDENTIALSID)
            }
            branch(BRANCH)
        }
    }
    
    triggers
    {
        scm('')
    }

    steps
    {
        shell("bash start.sh")
    }
}
