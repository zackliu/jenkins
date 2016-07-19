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
                credentials("67644618-a3e9-4742-8c2e-82479635844d")
            }
            branch("master")
        }
    }

    steps
    {
        shell("sudo node createjobs.js")
    }
}