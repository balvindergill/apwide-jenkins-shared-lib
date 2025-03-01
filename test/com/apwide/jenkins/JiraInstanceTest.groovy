package com.apwide.jenkins

import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.golive.Golive
import com.apwide.jenkins.jira.Project
import com.apwide.jenkins.jira.Version
import com.apwide.jenkins.util.MockHttpRequestPlugin
import com.apwide.jenkins.util.MockPipelineScript
import com.apwide.jenkins.util.MockReadJsonPlugin
import com.apwide.jenkins.util.Parameters
import spock.lang.Specification

class JiraInstanceTest extends Specification {

    private final script = new MockPipelineScript(new MockHttpRequestPlugin(), new MockReadJsonPlugin())
    private final jiraConfig = [
            jiraBaseUrl: 'http://192.168.0.6:8080',
            jiraCredentialsId: 'localhost-jira-admin'
    ]
    private final env = [
            JIRA_BASE_URL: 'http://192.168.0.6:8080',
            JIRA_CREDENTIALS_ID: 'localhost-jira-admin'
    ]

    def "get list of projects"() {
        given:
        def project = new Project(script, jiraConfig)

        when:
        def projects = project.getAll()

        then:
        projects != null
        projects.size() > 1
    }

    def "get list of environments"() {
        given:
        def golive = new Environments(script, jiraConfig)

        when:
        def environments = golive.findAll()

        then:
        environments != null
        environments.size() > 1
    }

    def "get list of environments for eCommerce application"() {
        given:
        def golive = new Environments(script, jiraConfig)

        when:
        def environments = golive.findAll application:'eCommerce'

        then:
        environments != null
        environments.size() > 1
    }

    def "search for environments"() {
        given:
        def environments = new Environments(script, jiraConfig)

        when:
        def result = environments.search([
                applicationName:'eCommerce',
                statusName:['Up', 'Down'],
                '# servers':'4'
        ])

        then:
        result != null
        result.size() > 1
    }

    def "with environments"() {
        given:
        def environments = new Environments(script, jiraConfig)

        when:
        environments.withEnvironments([
                applicationName:'eCommerce',
                statusName:['Up', 'Down'],
                '# servers':'4'
        ]) { environment -> script.echo environment.url }

        then:
        true
    }

    def "get version information ECOM-2.20"() {
        given:
        def version = new Version(script, jiraConfig)

        when:
        def foundVersion = version.get("10108")

        then:
        foundVersion instanceof Map
    }

    def "get project versions"() {
        given:
        def project = new Project(script, jiraConfig)

        when:
        def foundVersions = project.versions('BUBU')

        then:
        foundVersions != null
    }

    def "create version"() {
        given:
        def version = new Version(script, jiraConfig)
        def versionName = "Pipeline Version ${new Date().getTime()}"
        when:
        def createdVersion = version.create(
                description: 'An excellent version',
                name: versionName,
                project: 'BUBU'
        )

        then:
        createdVersion instanceof Map
        createdVersion.name == versionName
    }

    def "check environment status"() {
        given:
        def environment = new Environment(script, jiraConfig)

        when:
        def updatedStatus = environment.checkAndUpdateStatus('Jira', 'Dev', 'Down', 'Up')

        then:
        updatedStatus != null
    }

    def "concat map"() {
        given:
        Map params = [
                httpMethod: 'GET',
                path      : '/path',
                body      : null
        ]

        when:
        Map mergedConfig = (jiraConfig?: []) << (params?: [])
        Parameters parameters = new Parameters([env: env], mergedConfig)

        then:
        parameters != null
    }

    def "create environment and categories"() {
        given:
        def golive = new Golive(script, jiraConfig)

        when:
        def env = golive.createEnvironmentAndCategoryIfNotExist(
                'eCommerce',
                'Test2',
                'Default Environment Permission Scheme')

        then:
        env != null
    }

    def "create environment"() {
        given:
        def environment = new Environment(script, jiraConfig)

        when:
        def env = environment.create(
                'eCommerce',
                'Test2',
                'Default Environment Permission Scheme')

        then:
        env != null
    }
}
