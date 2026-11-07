pipeline {
  agent {
    label 'jdk25 && maven && docker'
  }
  environment {
    CODECOV_TOKEN = credentials('config-server-codecov-token')
    NAME='config-server'
    DOCKER_IMAGE = 'bremersee/config-server'
    DOCKER_HOSTS = 'app1 app2'
    DOCKER_PUSH=false
    DEPLOY_ON_APP_SERVER=false
    SITE = true
    DEPLOY_TO_DEBIAN_REPO = false
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
    stage('Site') {
      when {
        allOf {
          environment name: 'SITE', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
            branch 'feature/*'
            branch 'bugfix/*'
          }
        }
      }
      steps {
        sh '''
          mvn -B -P build-system,gh-pages-site site site:stage
          git clone -b gh-pages git@github.com:bremersee/$NAME.git target/gh-pages
          cp -rf target/staging/* target/gh-pages
          git -C target/gh-pages add .
          git -C target/gh-pages commit -m "Maven site"
          git -C target/gh-pages push
          rm -rf target/gh-pages
        '''
      }
      post {
        always {
          sh 'curl -s https://codecov.io/bash | bash -s - -t ${CODECOV_TOKEN}'
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
              docker -H ssh://root@\$DOCKER_HOST pull $DOCKER_IMAGE:\$TAG
              docker \
                -H ssh://root@\$DOCKER_HOST \
                run \
                --detach \
                --tty \
                --interactive \
                --name=$NAME \
                --restart=unless-stopped \
                -e ACTIVE_PROFILES='docker,simple-ldap' \
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
    stage('Deploy to Debian Repo') {
      when {
        allOf {
          environment name: 'DEPLOY_TO_DEBIAN_REPO', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
          }
        }
      }
      steps {
        sh '''
          mvn -B -DskipTests=true -P build-system,debian,deploy-to-debian-repo clean deploy
        '''
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