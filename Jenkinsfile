pipeline {
  agent none
  stages {
    stage('Build') {
      agent {
        label 'maven'
      }
      steps {
        sh 'mvn clean compile'
      }
    }
    stage('Test') {
      agent {
        label 'maven'
      }
      steps {
        sh 'mvn test'
      }
    }
    stage('Push') {
      agent {
        label 'maven'
      }
      steps {
        sh 'mvn -DskipTests -Ddockerfile.skip=false package dockerfile:push'
      }
    }
    stage('Push latest') {
      agent {
        label 'maven'
      }
      steps {
        sh 'mvn -DskipTests -Ddockerfile.skip=false -Ddockerfile.tag=latest package dockerfile:push'
      }
    }
    stage('Site') {
      agent {
        label 'maven'
      }
      steps {
        sh 'mvn site-deploy'
      }
    }
    stage('Deploy on dev-swarm') {
      agent {
        label 'dev-swarm'
      }
      when {
        branch 'develop'
      }
      steps {
        echo 'Deploying docker image.'
        sh 'docker service update --image bremersee/config-server:latest config-server'
      }
    }
  }
}