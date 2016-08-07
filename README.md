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
		cd jenkins/jenkins-master
		docker build -t jenkinsmaster .

####csharp environment
This docker image is used to build .net, run .net and run node.js
		cd mono
		docker build -t csharpslave .

####protractor environment
This docker image is used to run protractor test
		cd e2e
		docker build -t protractorslave .

###Run Jenkins
You can run your jenkins with
		docker run -d -p 8080:8080 jenkinsmaster

###How to configure your Jenkins
The main config file is config.json
You can specify your url(It's very important and don't use local url), credentials, administrator, plugins and workflow
If you want to config it after Jenkins init, you can run node file in autojenkins folder.

###How to configure your jobs
You can add some json files or groovy files in repo, and then you must specify these file in config.json. Once you commit, Jenkins will
be triggered and update all the jobs.

###Change repo
It may be complex if you want to change a job-config repo. You need to change url of config file in each groovy file and node file.

###Known issues
I found that raw.github.com may not change at once you do a commit or your repo may not support raw text. You should change the line
"def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();" into what I've written
in createjobs/createjobs.groovy.
I will fix it later.