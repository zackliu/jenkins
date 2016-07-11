/*
   Created by Sam Gleske
   Automatically configure the docker cloud stack in Jenkins.
 */
//import com.nirima.jenkins.plugins.docker
//import com.nirima.jenkins.plugins.docker.DockerCloud
//import com.nirima.jenkins.plugins.docker.DockerTemplate
import com.nirima.jenkins.plugins.docker.*
import com.nirima.jenkins.plugins.docker.launcher.*
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.kohsuke.stapler.StaplerRequest
import hudson.plugins.sshslaves.SSHConnector
import hudson.plugins.sshslaves.SSHLauncher

JSONObject docker_settings = new JSONObject()
docker_settings.putAll([
    name: 'docker-local',
    serverUrl: 'http://127.0.0.1:4243',
    containerCapStr: '50',
    connectionTimeout: 5,
    readTimeout: 15,
    credentialsId: '',
    templates: [
        [
            image: 'csharp',
            dnsString: '',
            network: '',
            dockerCommand: '',
            volumesString: '',
            volumesFromString: '',
            environmentsString: '',
            lxcConfString: '',
            hostname: '',
            memoryLimit: null,
            memorySwap: null,
            cpuShares: null,
            bindPorts: '',
            bindAllPorts: false,
            privileged : false,
            tty: false,
            macAddress: '',

            labelString: 'csharp_slave',
            remoteFs: '/home/jenkins',
            remoteFsMapping: '/home/jenkins',
            instanceCapStr: '1',
            numExecutors: 1,
            removeVolumes: false,
            pullStrategy: "Pull once and update latest"
        ]
    ]
])

def bindJSONToList( Class type, Object src) {
    if(type == DockerTemplate){
        ArrayList<DockerTemplate> r = new ArrayList<DockerTemplate>();
        if (src instanceof JSONObject) {
            JSONObject temp = (JSONObject) src;
            def dtb = new DockerTemplateBase(
                                            temp.optString("image"),
                                            temp.optString("dnsString"), //dnsString
                                            //temp.optString("network"), //network
                                            temp.optString("dockerCommand"), //dockerCommand
                                            temp.optString("volumesString"), //volumesString
                                            temp.optString("volumesFromString"), //volumesFromString
                                            temp.optString("environmentsString"), //environmentsString
                                            temp.optString("lxcConfString"), //lxcConfString
                                            temp.optString("hostname"), //hostname
                                            temp.optInt("memoryLimit"),
                                            temp.optInt("memorySwap"),
                                            temp.optInt("cpuShares"),
                                            temp.optString("bindPorts"),
                                            temp.optBoolean("bindAllPorts"),
                                            temp.optBoolean("privileged"),
                                            temp.optBoolean("tty"),
                                            temp.optString("macAddress")
                                            );
            r.add(
                    new DockerTemplate(dtb,
                                       temp.optString("labelString"),
                                       temp.optString("remoteFs"),
                                       temp.optString("remoteFsMapping"),
                                       temp.optString("instanceCapStr"),
                                       Node.Mode.EXCLUSIVE, 
                                       temp.optInt("numExecutors"),
                                       new DockerComputerSSHLauncher(new SSHConnector(22, "jenkins-slave-password", "", "", "", "", null, 0, 0)),
                                       null,
                                       temp.optBoolean("removeVolumes"),
                                       DockerImagePullStrategy.PULL_LATEST
                )
            );
        }
        if (src instanceof JSONArray) {
            JSONArray json_array = (JSONArray) src;
            for (Object o : json_array) {
                if (o instanceof JSONObject) {
                    JSONObject temp = (JSONObject) o;
                    def dtb = new DockerTemplateBase(
                                            temp.optString("image"),
                                            temp.optString("dnsString"), //dnsString
                                           // temp.optString("network"), //network
                                            temp.optString("dockerCommand"), //dockerCommand
                                            temp.optString("volumesString"), //volumesString
                                            temp.optString("volumesFromString"), //volumesFromString
                                            temp.optString("environmentsString"), //environmentsString
                                            temp.optString("lxcConfString"), //lxcConfString
                                            temp.optString("hostname"), //hostname
                                            temp.optInt("memoryLimit"),
                                            temp.optInt("memorySwap"),
                                            temp.optInt("cpuShares"),
                                            temp.optString("bindPorts"),
                                            temp.optBoolean("bindAllPorts"),
                                            temp.optBoolean("privileged"),
                                            temp.optBoolean("tty"),
                                            temp.optString("macAddress")
                                            );
                    r.add(
                            new DockerTemplate(
                                        dtb,
                                       temp.optString("labelString"),
                                       temp.optString("remoteFs"),
                                       temp.optString("remoteFsMapping"),
                                       temp.optString("instanceCapStr"),
                                       Node.Mode.EXCLUSIVE, 
                                       temp.optInt("numExecutors"),
                                       new DockerComputerSSHLauncher(new SSHConnector(22, "jenkins-slave-password", "", "", "", "", null, 0, 0)),
                                       null,
                                       temp.optBoolean("removeVolumes"),
                                       DockerImagePullStrategy.PULL_LATEST
                            )
                    );
                }
            }
        }
        return r;
    }
    if(type == DockerCloud){
        ArrayList<DockerCloud> r = new ArrayList<DockerCloud>();
        if (src instanceof JSONObject) {
            JSONObject temp = (JSONObject) src;
            r.add(
                new DockerCloud(temp.optString("name"),
                                bindJSONToList(DockerTemplate.class, temp.optJSONArray("templates")),
                                temp.optString("serverUrl"),
                                temp.optString("containerCapStr"),
                                temp.optInt("connectTimeout", 5),
                                temp.optInt("readTimeout", 15),
                                temp.optString("credentialsId"),
                                temp.optString("version")
                )
            );
        }
        if (src instanceof JSONArray) {
            JSONArray json_array = (JSONArray) src;
            for (Object o : json_array) {
                if (o instanceof JSONObject) {
                    JSONObject temp = (JSONObject) src;
                    r.add(
                        new DockerCloud(temp.optString("name"),
                                        bindJSONToList(DockerTemplate.class, temp.optJSONArray("templates")),
                                        temp.optString("serverUrl"),
                                        temp.optString("containerCapStr"),
                                        temp.optInt("connectTimeout", 5),
                                        temp.optInt("readTimeout", 15),
                                        temp.optString("credentialsId"),
                                        temp.optString("version")
                        )
                    );
                }
            }
        }
        return r;
    }
}

def req = [
    bindJSONToList: { Class type, Object src ->
        bindJSONToList(type, src)
    }
] as org.kohsuke.stapler.StaplerRequest

if(!Jenkins.instance.clouds.getByName('docker-local')) {
  println 'Adding docker cloud'
  Jenkins.instance.clouds.addAll(req.bindJSONToList(DockerCloud.class, docker_settings))
}

