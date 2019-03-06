pipeline {
  agent none
  stages {
    stage('Deploy on dev-swarm') {
      agent {
        label 'dev-swarm'
      }
      when {
        branch 'develop'
      }
      steps {
        echo 'Deploying docker image.'
        sh 'docker service update --image bremersee/groupman:latest groupman'
      }
    }
  }
}