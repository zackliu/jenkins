import com.nirima.jenkins.plugins.docker.*
import com.nirima.jenkins.plugins.docker.launcher.*
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.kohsuke.stapler.StaplerRequest
import hudson.plugins.sshslaves.SSHConnector
import hudson.plugins.sshslaves.SSHLauncher
import hudson.model.Hudson
import hudson.slaves.Cloud
import java.net.*
import groovy.json.JsonSlurper

def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);

def bindJSONToList( Class type, def src) {
    if(type == DockerTemplate){
        ArrayList<DockerTemplate> r = new ArrayList<DockerTemplate>();
        
        src.each{
            dockertemp ->
            r.add(
                new DockerTemplate(
                    new DockerTemplateBase(
                        dockertemp.image,
                        dockertemp.dnsString,
                        dockertemp.dockerCommand,
                        dockertemp.volumesString,
                        dockertemp.volumesFromString,
                        dockertemp.environmentsString,
                        dockertemp.lxcConfString,
                        dockertemp.hostname,
                        dockertemp.memoryLimit,
                        dockertemp.memorySwap,
                        dockertemp.cpuShares,
                        dockertemp.bindPorts,
                        dockertemp.bindAllPorts,
                        dockertemp.privileged,
                        dockertemp.tty,
                        dockertemp.macAddress
                    ),
                    dockertemp.labelString,
                    dockertemp.remoteFs,
                    dockertemp.remoteFsMapping,
                    dockertemp.instanceCapStr,
                    Node.Mode.EXCLUSIVE,
                    dockertemp.numExecutors,
                    new DockerComputerSSHLauncher(new SSHConnector(22, "jenkins-slave-password", "", "", "", "", null, 0, 0)),
                    null,
                    false,
                    DockerImagePullStrategy.PULL_LATEST
                )
            )
        }
        println(r)
        return r;
    }
    if(type == DockerCloud){
        ArrayList<DockerCloud> r = new ArrayList<DockerCloud>();
        src.each{dockercloud ->
            //println(Jenkins.getInstance().clouds.getByName("dcloud"))

            if(Jenkins.getInstance().clouds.getByName(dockercloud.name))
            {
                Jenkins.getInstance().clouds.remove(Jenkins.instance.clouds.getByName(dockercloud.name));
            }

            r.add(
                new DockerCloud(
                    dockercloud.name,
                    bindJSONToList(DockerTemplate.class, dockercloud.templates),
                    dockercloud.severUrl,
                    dockercloud.containerCapStr,
                    dockercloud.connectTimeout,
                    dockercloud.readTimeout,
                    dockercloud.credentialsId,
                    null
                )
            )
        }
        return r;
    }
}

def req = [
    bindJSONToList: { Class type, def src ->
        bindJSONToList(type, src)
    }
] as org.kohsuke.stapler.StaplerRequest


  Jenkins.instance.clouds.addAll(req.bindJSONToList(DockerCloud.class, state.docker))


