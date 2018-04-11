#!groovy

import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.AdminWhitelistRule

def instance = Jenkins.getInstance()

//def user = new File("/run/secrets/jenkins-user").text.trim()
//def pass = new File("/run/secrets/jenkins-pass").text.trim()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount('admin', 'admin')
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)

Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)

def updateCenter = instance.getUpdateCenter()
updateCenter.updateAllSites()
def pluginManager = instance.getPluginManager()

def deployingPlugins = []
(System.getenv()['JENKINS_PLUGINS'] ?: '').split().each { String pluginName ->
    if (pluginManager.getPlugin(pluginName)) {
        return
    }
    def plugin = updateCenter.getPlugin(pluginName)
    if (!plugin) {
        return
    }
    deployingPlugins.add(plugin.deploy())
}

deployingPlugins.each { def deployingPlugin ->
    deployingPlugin.get()
}

instance.save()
if (deployingPlugins) {
    instance.restart()
}

