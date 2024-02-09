pipeline {
  agent {
    label 'docker && maven'
  }
  environment {
    CODECOV_TOKEN = credentials('config-server-codecov-token')
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
    stage('Build') {
      steps {
        sh 'mvn -B clean package'
      }
      post {
        always {
          junit '**/surefire-reports/*.xml'
          jacoco(
              execPattern: '**/coverage-reports/*.exec'
          )
          sh 'curl -s https://codecov.io/bash | bash -s - -t ${CODECOV_TOKEN}'
        }
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
          sh '''
            #!/bin/bash
            cp $KS target/keystore.jks
            docker \
              -H $DOCKER_HOST \
              --tlsverify \
              --tlscert=$DOCKER_CERT \
              --tlskey=$DOCKER_KEY \
              --tlscacert=$DOCKER_CA \
              build \
              -t bremersee/config-server-configured-arm64:snapshot-${BUILD_NUMBER} \
              -f DockerfileConfiguredArm64 \
              --build-arg platform=arm64 \
              --build-arg keystore=target/keystore.jks \
              --build-arg keystoreType=jks \
              --build-arg keystorePassword=$KS_PASSWORD \
              --build-arg keystoreAlias=$ALIAS \
              --build-arg keystoreSecret=$SECRET \
              --build-arg clientUser=$CLIENT \
              --build-arg clientPassword=$CLIENT_PASSWORD \
              --build-arg actuatorUser=$ACTUATOR \
              --build-arg actuatorPassword=$ACTUATOR_PASSWORD \
              --build-arg adminUser=$ADMIN \
              --build-arg adminPassword=$ADMIN_PASSWORD \
              .
              rm target/keystore.jks
              CONTAINER_ID=\$(docker \
                -H $DOCKER_HOST \
                --tlsverify \
                --tlscert=$DOCKER_CERT \
                --tlskey=$DOCKER_KEY \
                --tlscacert=$DOCKER_CA \
                ps -aqf "name=^config-server")
                echo "Container id is \$CONTAINER_ID"
              if [ -z "\$CONTAINER_ID" ]; then
                echo "No config-server container is running."
              else
                echo "Stopping config-server container with ID \$CONTAINER_ID."
              fi
              sleep 10
              NUMBER=0
              NEW_IMAGE=\$(docker \
                -H $DOCKER_HOST \
                --tlsverify \
                --tlscert=$DOCKER_CERT \
                --tlskey=$DOCKER_KEY \
                --tlscacert=$DOCKER_CA \
                images | awk '/config-server-configured-arm64/ && /snapshot-${BUILD_NUMBER}/')
              while [ -z "\$NEW_IMAGE" ] -a [ \$NUMBER -lt 3 ]; do
                echo "\$NUMBER: No new config-server image found. Waiting 10 seconds and trying again."
                ((NUMBER=\$NUMBER+1))
                sleep 10
                NEW_IMAGE=\$(docker \
                  -H $DOCKER_HOST \
                  --tlsverify \
                  --tlscert=$DOCKER_CERT \
                  --tlskey=$DOCKER_KEY \
                  --tlscacert=$DOCKER_CA \
                  images | awk '/config-server-configured-arm64/ && /snapshot-${BUILD_NUMBER}/')
              done
              if [ -z "\$NEW_IMAGE" ]; then
                echo "New config-server image was found. Giving up."
                exit 1
              fi
              echo "New config-server image is present: \$NEW_IMAGE"
          '''
        }
      }
    }
  }
}