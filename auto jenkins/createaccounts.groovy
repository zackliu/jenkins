import hudson.security.*
import java.net.*
import groovy.json.JsonSlurper


def jsonPayload = new URL("https://raw.githubusercontent.com/zackliu/jenkins/master/config.json").getText();
def state = new JsonSlurper().parseText(jsonPayload);

checkedTimes = 3

def checkStarted()
{
  if(Jenkins == null)
  {
    checkedTimes = checkedTimes - 1
    if(checkedTimes <= 0) exit(1)
    sleep(5000)
    checkStarted()
  }
  return
}

checkStarted()


def instance = Jenkins.getInstance();
def hudsonRealm = new HudsonPrivateSecurityRealm(false);
for(pairs in state.user)
{
    hudsonRealm.createAccount(pairs['username'], pairs['password']);
}
 instance.setSecurityRealm(hudsonRealm);
//hudsonRealm.createAccount("Testaccount", "Testaccount");
//instance.setSecurityRealm(hudsonRealm);

//版本太旧，下面方法不能实现
//def strategy = new FullControlOnceLoggedInAuthorizationStrategy();
//strategy.isAllowAnonymousRead();
//instance.setAuthorizationStrategy(strategy);


def strategy = new GlobalMatrixAuthorizationStrategy()
for(pairs in state.administrator)
{
    strategy.add(Jenkins.ADMINISTER, pairs['username']);
}
for(pairs in state.user)
{
    strategy.add(Jenkins.READ,pairs['username']);
    jenkins.model.Jenkins.getInstance().getItems().each{
        item -> 
        strategy.add(item.READ,pairs['username']);
        strategy.add(item.BUILD,pairs['username']);
    }
}
//strategy.add(Jenkins.ADMINISTER, "Testaccount")
instance.setAuthorizationStrategy(strategy)

instance.save();