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
      steps {
        echo 'Deploying docker image.'
        def pom = readMavenPom file: 'pom.xml'
        echo "${pom.version}"
        sh 'docker service ls'
      }
    }
  }
}