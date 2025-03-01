import com.apwide.jenkins.jira.Project

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, parameters ->
        return new Project(this, parameters.config).get(parameters.params.id)
    }
}