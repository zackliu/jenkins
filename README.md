利用jenkins,docker进行自动化部署
=================================

安装方式（处于测试阶段，无法保证安装成功 ）
------------


##jenkins:
        jenkins-data:用于生成data-volume  
        生成：docker build -t jenkinsdata jenkins-data/.
        运行：docker run --name=jenkins-data jenkinsdata

        jenkins-master:用于生成jenkins主体
        生成：docker build -t jenkinsmaster jenkins-master/.
        运行: docker run -p 80:8080 -p 50000:50000 --name=jenkins-master --volumes-from=jenkins-data -d jinkinsmaster

        成功运行后添加插件：docker plugins, github pulgins

##node-slave:
        node-slave用于测试node
        生成：docker build -t nodeslave node-slave/.
        不需要手动运行

##csharp-slave:
        生成：docker build -t csharpslave csharp/.

##protractor:
        生成：docker build -t protractor e2e/.

Jenkins中参数配置
--------------
##Credentials设置
        建立doman为global的credentials，选择Username with password，输入node-slave中Jenkins账户的用户名和密码。

##Cloud设置
        选择Docker，Name自定，Docker URL:需要能够读到Docker API的地址，目前docker运行在ACS上，找到docker swarm的地址
        此处为http://172.16.0.5:2375
        Label假设为node_slave
        Image选择nodeslave，Remote Filing System Root，Remote FS Root Mapping都设置为/home/jenkins
        Credentials选择之前创立的

##自动配置方式（NEW）
		使用auto jenkins中的js文件进行配置
		createaccounts: 创建账户
		createcredentials：创建credentials
		createdocker: 建立docker template
		installplugins: 更新和安装插件
		createjobs：自动配置job（未完成）
		
		使用config.json进行配置



创立新JOB
----------------
        Restrict where this project can be run选择node_slave即可。

后续工作
--------------
        完成Job-DSL config
