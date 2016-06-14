***************有关这个项目*****************
本项目利用jenkins,docker进行自动化部署

jenkins:
    jenkins-data:用于生成data-volume  
        生成：docker build -t jenkinsdata jenkins-data/.
        运行：docker run --name=jenkins-data jenkinsdata
    jenkins-master:用于生成jenkins主体
        生成：docker build -t jenkinsmaster jenkins-master/.
        运行: docker run -p 8080:8080 -p 50000:50000 --name=jenkins-master --volumes-from=jenkins-data -d jinkinsmaster

成功运行后添加插件：docker plugins, github pulgins
在新项目中 BUILD设置Execute shell:
rm -rf /var/jenkins_home/jobs/test/workspace/test.tar.gz
rm -rf /var/jenkins_home/jobs/test/workspace/Dockerfile
tar -zcvf /tmp/test.tar.gz /var/jenkins_home/jobs/test/workspace/
rm -rf /var/jenkins_home/jobs/test/workspace/*
mv /tmp/test.tar.gz /var/jenkins_home/jobs/test/workspace/
echo "--------files tar ok---------"
touch /var/jenkins_home/jobs/test/workspace/Dockerfile
cat>Dockerfile<<EOF
FROM node:argon
COPY ./test.tar.gz /srv/work
EOF
echo "--------Dockerfile OK---------"
