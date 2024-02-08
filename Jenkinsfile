pipeline {
  agent {
    label 'docker && maven'
  }
  environment {
    TEST = false
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
      steps {
        withCredentials([
          string(credentialsId: 'data-docker-host', variable: 'DOCKER_HOST'),
          file(credentialsId: 'data-docker-ca', variable: 'DOCKER_CA'),
          file(credentialsId: 'data-docker-cert', variable: 'DOCKER_CERT'),
          file(credentialsId: 'data-docker-key', variable: 'DOCKER_KEY'),
          file(credentialsId: 'config-server-keystore', variable: 'KS'),
          string(credentialsId: 'config-server-keystore-password', variable: 'KS_PASSWORD'),
          usernamePassword(credentialsId: 'config-server-keystore-entry', usernameVariable: 'ALIAS', passwordVariable: 'SECRET'),
          usernamePassword(credentialsId: 'config-server-client', usernameVariable: 'CLIENT', passwordVariable: 'CLIENT_PASSWORD'),
          usernamePassword(credentialsId: 'config-server-actuator', usernameVariable: 'ACTUATOR', passwordVariable: 'ACTUATOR_PASSWORD'),
          usernamePassword(credentialsId: 'config-server-admin', usernameVariable: 'ADMIN', passwordVariable: 'ADMIN_PASSWORD')
        ]) {
          sh 'cp $KS keystore.jks'
          sh 'echo "docker -H $DOCKER_HOST --tlsverify --tlscert=$DOCKER_CERT --tlskey=$DOCKER_KEY --tlscacert=$DOCKER_CA build -t bremersee/config-server-ks:snapshot -f DockerfileConfigured --build-arg platform=arm64 --build-arg keystore=ks.jks --build-arg keystoreType=jks --build-arg keystorePassword=$KS_PASSWORD --build-arg keystoreAlias=$ALIAS --build-arg keystoreSecret=$SECRET --build-arg clientUser=$CLIENT --build-arg clientPassword=$CLIENT_PASSWORD --build-arg actuatorUser=$ACTUATOR --build-arg actuatorPassword=$ACTUATOR_PASSWORD --build-arg adminUser=$ADMIN --build-arg adminPassword=$ADMIN_PASSWORD" > docker.txt'
          sh 'echo $(cat docker.txt)'
          sh 'docker -H $DOCKER_HOST --tlsverify --tlscert=$DOCKER_CERT --tlskey=$DOCKER_KEY --tlscacert=$DOCKER_CA build -t bremersee/config-server-ks:snapshot -f DockerfileConfigured --build-arg platform=arm64 --build-arg keystore=keystore.jks --build-arg keystoreType=jks --build-arg keystorePassword=$KS_PASSWORD --build-arg keystoreAlias=$ALIAS --build-arg keystoreSecret=$SECRET --build-arg clientUser=$CLIENT --build-arg clientPassword=$CLIENT_PASSWORD --build-arg actuatorUser=$ACTUATOR --build-arg actuatorPassword=$ACTUATOR_PASSWORD --build-arg adminUser=$ADMIN --build-arg adminPassword=$ADMIN_PASSWORD .'
        }
      }
    }
  }
}