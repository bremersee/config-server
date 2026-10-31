pipeline {
  agent {
    label 'jdk25 && maven && docker'
  }
  environment {
    CODECOV_TOKEN = credentials('config-server-codecov-token')
    NAME='config-server'
    DOCKER_IMAGE = 'bremersee/config-server'
    DOCKER_HOSTS = 'app1 app2'
    DOCKER_PUSH=true
    DEPLOY_ON_APP_SERVER=true
    SNAPSHOT_SITE = false
    RELEASE_SITE = false
    DEPLOY_SNAPSHOT_ON_DATA = false
    DEPLOY_RELEASE_ON_DATA = false
    DEPLOY_SNAPSHOT_ON_SERVER = false
    DEPLOY_RELEASE_ON_SERVER = false
    DEPLOY_RELEASE_ON_REPOSITORY_DEBIAN_BULLSEYE = false
    CLEAN = true
  }
  tools {
    jdk 'jdk25'
    maven 'm3'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '8', artifactNumToKeepStr: '8'))
  }
  stages {
    stage('Tools') {
      steps {
        sh 'java -version'
        sh 'mvn -B --version'
      }
    }
    stage('Build') {
      steps {
        sh 'mvn -B -P build-system clean package'
      }
      post {
        always {
          junit '**/surefire-reports/*.xml'
          recordCoverage(
              tools: [[parser: 'JACOCO', pattern: '**/coverage-reports/*.exec']],
              sourceCodeRetention: 'LAST_BUILD'
          )
        }
      }
    }
    stage('Docker Push') {
      when {
        allOf {
          environment name: 'DOCKER_PUSH', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
          }
        }
      }
      steps {
        withCredentials([
          usernamePassword(
            credentialsId: 'dockerhub',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_SECRET')
        ]) {
          sh '''
            #!/bin/bash
            MVN_VERSION=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
            TAG=\$MVN_VERSION
            LATEST="latest"
            echo "Building and pushing docker image $DOCKER_IMAGE:\$TAG"
            mvn -B -P build-system -DskipTests clean package
            docker context use default
            docker login -u $DOCKER_USER -p $DOCKER_SECRET
            docker run --privileged --rm tonistiigi/binfmt --install all
            docker \
              buildx \
              build \
              -t $DOCKER_IMAGE:\$TAG \
              -t $DOCKER_IMAGE:\$LATEST \
              -f Dockerfile \
              --platform=linux/amd64,linux/arm64 \
              --push \
              .
            docker image prune -a -f
          '''
        }
      }
    }
    stage('Deploy On App Server') {
      when {
        allOf {
          environment name: 'DOCKER_PUSH', value: 'true'
          environment name: 'DEPLOY_ON_APP_SERVER', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
          }
        }
      }
      steps {
        withCredentials([
          string(credentialsId: 'config-server-key', variable: 'ENCRYPTION_KEY')
        ]) {
          sh '''
            #!/bin/bash
            sleep 10
            MVN_VERSION=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
            TAG=\$MVN_VERSION
            LATEST="latest"
            for DOCKER_HOST in $DOCKER_HOSTS; do
              CONTAINER_ID=\$(docker \
                -H ssh://root@\$DOCKER_HOST \
                ps -aqf "name=^$NAME")
              echo "Container id is \$CONTAINER_ID"
              if [ -z "\$CONTAINER_ID" ]; then
                echo "No $NAME container is running."
              else
                echo "Stopping $NAME container with ID \$CONTAINER_ID."
                docker \
                  -H ssh://root@\$DOCKER_HOST \
                  stop \$CONTAINER_ID
                sleep 10
                docker \
                  -H ssh://root@\$DOCKER_HOST \
                  rm \$CONTAINER_ID
                sleep 10
              fi
              docker \
                -H ssh://root@\$DOCKER_HOST \
                run \
                --detach \
                --tty \
                --interactive \
                --name=$NAME \
                --restart=unless-stopped \
                -e ACTIVE_PROFILES='docker,ldap' \
                -e ENCRYPTION_KEY='$ENCRYPTION_KEY' \
                -p 10100:8080 \
                $DOCKER_IMAGE:\$TAG
              sleep 10
              docker \
                -H ssh://root@\$DOCKER_HOST \
                image prune -a -f
            done
          '''
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
          // usernamePassword(credentialsId: 'config-server-client', usernameVariable: 'CLIENT', passwordVariable: 'CLIENT_PASSWORD'),
          // usernamePassword(credentialsId: 'config-server-actuator', usernameVariable: 'ACTUATOR', passwordVariable: 'ACTUATOR_PASSWORD'),
          // usernamePassword(credentialsId: 'config-server-admin', usernameVariable: 'ADMIN', passwordVariable: 'ADMIN_PASSWORD')
        ]) {
//              --build-arg clientUser=$CLIENT \
//              --build-arg clientPassword=$CLIENT_PASSWORD \
//              --build-arg actuatorUser=$ACTUATOR \
//              --build-arg actuatorPassword=$ACTUATOR_PASSWORD \
//              --build-arg adminUser=$ADMIN \
//              --build-arg adminPassword=$ADMIN_PASSWORD \
          sh '''
            #!/bin/bash
            cp $KS target/keystore.jks
            IMAGE=bremersee/config-server-configured-arm64
            TAG=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)-${BUILD_NUMBER}
            docker \
              -H $DOCKER_HOST \
              --tlsverify \
              --tlscert=$DOCKER_CERT \
              --tlskey=$DOCKER_KEY \
              --tlscacert=$DOCKER_CA \
              build \
              -t \$IMAGE:\$TAG \
              -f DockerfileConfiguredArm64 \
              --build-arg platform=arm64 \
              --build-arg keystore=target/keystore.jks \
              --build-arg keystoreType=jks \
              --build-arg keystorePassword=$KS_PASSWORD \
              --build-arg keystoreAlias=$ALIAS \
              --build-arg keystoreSecret=$SECRET \
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
                docker \
                  -H $DOCKER_HOST \
                  --tlsverify \
                  --tlscert=$DOCKER_CERT \
                  --tlskey=$DOCKER_KEY \
                  --tlscacert=$DOCKER_CA \
                  stop \$CONTAINER_ID
                sleep 10
                docker \
                  -H $DOCKER_HOST \
                  --tlsverify \
                  --tlscert=$DOCKER_CERT \
                  --tlskey=$DOCKER_KEY \
                  --tlscacert=$DOCKER_CA \
                  rm \$CONTAINER_ID
              fi
              sleep 10
              docker \
                -H $DOCKER_HOST \
                --tlsverify \
                --tlscert=$DOCKER_CERT \
                --tlskey=$DOCKER_KEY \
                --tlscacert=$DOCKER_CA \
                run \
                --detach \
                --tty \
                --interactive \
                --name=config-server \
                --restart=unless-stopped \
                -v config_server_data:/data \
                -p 11061:8080 \
                \$IMAGE:\$TAG
              sleep 10
              docker \
                -H $DOCKER_HOST \
                --tlsverify \
                --tlscert=$DOCKER_CERT \
                --tlskey=$DOCKER_KEY \
                --tlscacert=$DOCKER_CA \
                image prune -a -f
          '''
        }
      }
    }
    stage('Deploy Release On Data') {
      when {
        allOf {
          environment name: 'DEPLOY_RELEASE_ON_DATA', value: 'true'
          branch 'main'
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
//          usernamePassword(credentialsId: 'config-server-client', usernameVariable: 'CLIENT', passwordVariable: 'CLIENT_PASSWORD'),
//          usernamePassword(credentialsId: 'config-server-actuator', usernameVariable: 'ACTUATOR', passwordVariable: 'ACTUATOR_PASSWORD'),
//          usernamePassword(credentialsId: 'config-server-admin', usernameVariable: 'ADMIN', passwordVariable: 'ADMIN_PASSWORD')
        ]) {
          sh '''
            #!/bin/bash
            cp $KS target/keystore.jks
            IMAGE=bremersee/config-server-configured-arm64
            TAG=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
            docker \
              -H $DOCKER_HOST \
              --tlsverify \
              --tlscert=$DOCKER_CERT \
              --tlskey=$DOCKER_KEY \
              --tlscacert=$DOCKER_CA \
              build \
              -t \$IMAGE:\$TAG \
              -f DockerfileConfiguredArm64 \
              --build-arg platform=arm64 \
              --build-arg keystore=target/keystore.jks \
              --build-arg keystoreType=jks \
              --build-arg keystorePassword=$KS_PASSWORD \
              --build-arg keystoreAlias=$ALIAS \
              --build-arg keystoreSecret=$SECRET \
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
                docker \
                  -H $DOCKER_HOST \
                  --tlsverify \
                  --tlscert=$DOCKER_CERT \
                  --tlskey=$DOCKER_KEY \
                  --tlscacert=$DOCKER_CA \
                  stop \$CONTAINER_ID
                sleep 10
                docker \
                  -H $DOCKER_HOST \
                  --tlsverify \
                  --tlscert=$DOCKER_CERT \
                  --tlskey=$DOCKER_KEY \
                  --tlscacert=$DOCKER_CA \
                  rm \$CONTAINER_ID
              fi
              sleep 10
              docker \
                -H $DOCKER_HOST \
                --tlsverify \
                --tlscert=$DOCKER_CERT \
                --tlskey=$DOCKER_KEY \
                --tlscacert=$DOCKER_CA \
                run \
                --detach \
                --tty \
                --interactive \
                --name=config-server \
                --restart=unless-stopped \
                -v config_server_data:/data \
                -p 11061:8080 \
                \$IMAGE:\$TAG
              sleep 10
              docker \
                -H $DOCKER_HOST \
                --tlsverify \
                --tlscert=$DOCKER_CERT \
                --tlskey=$DOCKER_KEY \
                --tlscacert=$DOCKER_CA \
                image prune -a -f
          '''
        }
      }
    }
    stage('Deploy snapshot on config-server') {
      when {
        allOf {
          branch 'develop'
          environment name: 'DEPLOY_SNAPSHOT_ON_SERVER', value: 'true'
        }
      }
      steps {
        sh 'mvn -B -DskipTests=true -Pdebian11,copy-to-and-install-on-config-server clean deploy'
      }
    }
    stage('Deploy release on config-server') {
      when {
        allOf {
          branch 'main'
          environment name: 'DEPLOY_RELEASE_ON_SERVER', value: 'true'
        }
      }
      steps {
        sh 'mvn -B -DskipTests=true -Pdebian11,copy-to-and-install-on-config-server clean deploy'
      }
    }
    stage('Deploy release on apt repository debian-bullseye') {
      when {
        allOf {
          branch 'main'
          environment name: 'DEPLOY_RELEASE_ON_REPOSITORY_DEBIAN_BULLSEYE', value: 'true'
        }
      }
      steps {
        sh 'mvn -B -DskipTests=true -Dhttp.protocol.expect-continue=true -Pdebian11,deploy-to-repo-debian-bullseye clean deploy'
      }
    }
    stage('Deploy snapshot site') {
      when {
        allOf {
          environment name: 'SNAPSHOT_SITE', value: 'true'
          anyOf {
            branch 'develop'
            branch 'feature/*'
          }
        }
      }
      steps {
        sh 'mvn -B clean site-deploy'
      }
      post {
        always {
          sh 'curl -s https://codecov.io/bash | bash -s - -t ${CODECOV_TOKEN}'
        }
      }
    }
    stage('Deploy release site') {
      when {
        allOf {
          branch 'main'
          environment name: 'RELEASE_SITE', value: 'true'
        }
      }
      steps {
        sh 'mvn -B -P gh-pages-site clean site site:stage scm-publish:publish-scm'
      }
      post {
        always {
          sh 'curl -s https://codecov.io/bash | bash -s - -t ${CODECOV_TOKEN}'
        }
      }
    }
    stage('Clean') {
      when {
        environment name: 'CLEAN', value: 'true'
      }
      steps {
        sh 'mvn -B clean'
      }
    }
  }
}