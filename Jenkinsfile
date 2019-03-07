pipeline {
  agent none
  environment {
    SERVICE_NAME='config-server'
    DOCKER_IMAGE='bremersee/config-server'
    DEV_TAG='latest'
    PROD_TAF='release'
  }
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
      when {
        branch 'develop'
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
        echo 'Updating service ${SERVICE_NAME} docker image ${DOCKER_IMAGE}:${DEV_TAG}.'
        sh 'docker service update --image ${DOCKER_IMAGE}:${DEV_TAG} ${SERVICE_NAME}'
      }
    }
  }
}