CI/CD with Jenkins and Docker 
=================================

##About
With this project, you can build up your jenkins in docker by command line.

You can specify your plugins, configurations by JSON.

You can maintain your jobs with source control. And once you push a new job config, jobs in Jenkins will be configured itself.

##Usage
First of all, please make sure you have installed Docker already.

###Build images
####jenkins
		cd jenkins
		docker build -t jenkins .

####csharp environment
This docker image is used to build .net, run .net and run node.js

		cd mono
		docker build -t mono_slave .

####protractor environment
This docker image is used to run protractor test

		cd e2e
		docker build -t e2e_slave .

####createJobs environment
This docker image is used to createJobs

		cd createJobs
		docker build -t createjobs .


###Run Jenkins
(Notice) you must specify you repo which contains system by --env REPO="your repo"

You can run your jenkins with

		docker run -d -p 8080:8080 --privileged=true --name jenkins --env REPO="https://github.com/zackliu/jenkins.git" jenkins

###CreateJobs
When you start a new jenkins, there is one job named jobMonitor, which can monitor the JSON file in you jobs repo.

The repo url will be written in environment variables by defult named MONITORREPO.

You can also run createJobs container manually

		docker run -d --env MONITORREPO="your job repo" URL="your jenkins url" createJobs

By the way, if you run this docker container manually, you must make sure there is a environment variable named MONITORREPO in jenkins.

###How to configure your Jenkins
The main config file is config.json

You can specify your url(It's very important and don't use local url), credentials, administrator, plugins and workflow.

If you want to config it after Jenkins init, you can run node file in autojenkins folder.

###How to configure your jobs
You can add some json files or groovy files in repo, and then you must specify these file in config.json. Once you commit, Jenkins will be triggered and update all the jobs.

###Change repo
It may be complex if you want to change a job-config repo. You need to change url of config file in each groovy file and node file.

