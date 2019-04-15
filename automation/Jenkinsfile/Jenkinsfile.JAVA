pipeline {
    agent { node { label 'sonarqube-upshift' } }
    options {
      skipDefaultCheckout true
    }
    triggers {
      cron('0 8 * * *')
    }
    stages {
        stage('Deploy') {
            steps {
                // clone project
                git url: 'https://github.com/shay6/sonar_test.git'
            }
        }
        stage('Analyse') {
            steps {
                // coverage tests initialization script
                dir('examples/java-test-repo'){
                sh '''mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent \
                  install -Dmaven.test.failure.ignore=true || true'''
                }
            }
        }
        stage('Report') {
            /*
            sonar runner parameters, set sources and baseDir to project home
            ========================

            projectKey (string): SonarQube project identification key (unique)
            projectName (string): SonarQube project name (NOT unique)
            projectVersion (string): SonarQube project version (unique)
            sources (string): source code home directory
            projectBaseDir (string): project home directory (same as sources)
            language (string): project language(java)
            inclusions (string): file inclusion pattern
            exclusions (string): file exclusion pattern
            login (string): SonarQube server user name
            password (string): SonarQube server user password
            java.binaries (string): REQUIRED Since SonarJava 4.12
             */
            steps {
              // initite pre-configured sonar scanner tool on project
              // 'sonarqube_prod' is our cnfigured tool name, see yours
              // in the Jenkins tool configuration
              // NOTE: pay attention we are using maven under sonar env
              withSonarQubeEnv('sonarqube_prod') {
                dir('examples/java-test-repo'){ 
                sh "mvn sonar:sonar"
                }
              }
            }
        }
    }
}
