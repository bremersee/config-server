pipeline {
  agent none
  stages {
    stage('DeployOnDev') {
      agent {
        label 'dev-swarm'
      }
      when {
        branch 'develop'
      }
      def pom = readMavenPom file: 'pom.xml'
      steps {
        echo 'Deploying docker image.'

        echo "${pom.version}"
        sh 'docker service ls'
      }
    }
  }
}