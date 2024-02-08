pipeline {
  agent {
    label 'docker && maven'
  }
  environment {
    TEST = true
    DEPLOY_SNAPSHOT_ON_DATA = true
    DEPLOY_RELEASE_ON_DATA = true

    SERVICE_NAME='config-server'
    DOCKER_IMAGE='bremersee/config-server'
    DATA_TAG='latest'

    DEV_TAG='snapshot'
    PROD_TAG='latest'
    PUSH_SNAPSHOT_DOCKER_IMAGE = false
    PUSH_RELEASE_DOCKER_IMAGE = true
    DEPLOY_SNAPSHOT_ON_SERVER = false
    DEPLOY_RELEASE_ON_SERVER = true
    DEPLOY_RELEASE_ON_REPOSITORY_DEBIAN_BULLSEYE = true
    SNAPSHOT_SITE = false
    RELEASE_SITE = true
  }
  tools {
    jdk 'jdk17'
    maven 'm3'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '8', artifactNumToKeepStr: '8'))
  }
  stages {
    stage('Tools') {
      agent {
        label 'maven'
      }
      tools {
        jdk 'jdk17'
        maven 'm3'
      }
      steps {
        sh 'java -version'
        sh 'mvn -B --version'
      }
    }
    stage('Test') {
      when {
        environment name: 'TEST', value: 'true'
      }
      steps {
        sh 'mvn -B clean test'
      }
      post {
        always {
          junit '**/surefire-reports/*.xml'
          jacoco(
              execPattern: '**/coverage-reports/*.exec'
          )
        }
      }
    }
    stage('Build') {
      when {
        anyOf {
          environment name: 'DEPLOY_SNAPSHOT_ON_DATA', value: 'true'
          environment name: 'DEPLOY_RELEASE_ON_DATA', value: 'true'
        }
      }
      steps {
        sh 'mvn -B -Dmaven.test.skip=true clean package'
      }
    }
    stage('Deploy Snapshot On Data') {
      when {
        allOf {
          environment name: 'DEPLOY_SNAPSHOT_ON_DATA', value: 'true'
          anyOf {
            branch 'develop'
            branch 'feature/*'
          }
        }
      }
      withCredentials([
        string(credentialsId: 'data-docker-host', variable: 'docker-host'),
        file(credentialsId: 'data-docker-ca', variable: 'docker-ca'),
        file(credentialsId: 'data-docker-cert', variable: 'docker-cert'),
        file(credentialsId: 'data-docker-key', variable: 'docker-key'),
        file(credentialsId: 'config-server-keystore', variable: 'ks'),
        string(credentialsId: 'config-server-keystore-password', variable: 'ks-password'),
        usernamePassword(credentialsId: 'config-server-keystore-entry', usernameVariable: 'alias', passwordVariable: 'secret'),
        usernamePassword(credentialsId: 'config-server-client', usernameVariable: 'client', passwordVariable: 'client-password'),
        usernamePassword(credentialsId: 'config-server-actuator', usernameVariable: 'actuator', passwordVariable: 'actuator-password'),
        usernamePassword(credentialsId: 'config-server-admin', usernameVariable: 'admin', passwordVariable: 'admin-password')
      ]) {
        sh 'docker -H $docker-host --tlsverify --tlscert=$docker-cert --tlskey=$docker-key --tlscacert=$docker-ca -t bremersee/config-server-ks:snapshot -f DockerfileWithKeystore --build-arg platform=arm64 --build-arg keystore=$ks --build-arg keystoreType=jks --build-arg keystoreAlias=$alias --build-arg keystoreSecret=$secret --build-arg clientUser=$client --build-arg clientPassword=$client-password --build-arg actuatorUser=$actuator --build-arg actuatorPassword=$actuator-password --build-arg adminUser=$admin --build-arg adminPassword=$admin-password .'
      }
    }
  }
}