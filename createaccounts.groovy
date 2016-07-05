import hudson.security.*

  
def instance = Jenkins.getInstance();
def hudsonRealm = new HudsonPrivateSecurityRealm(false);
hudsonRealm.createAccount("Testaccount", "Testaccount");
instance.setSecurityRealm(hudsonRealm);

//版本太旧，下面方法不能实现
//def strategy = new FullControlOnceLoggedInAuthorizationStrategy();
//strategy.isAllowAnonymousRead();
//instance.setAuthorizationStrategy(strategy);

def strategy = new GlobalMatrixAuthorizationStrategy()
strategy.add(Jenkins.ADMINISTER, "Testaccount")
instance.setAuthorizationStrategy(strategy)

instance.save();