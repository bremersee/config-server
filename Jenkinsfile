pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean compile'
      }
    }
    stage('Test') {
      steps {
        sh 'mvn test'
      }
    }
    stage('Deploy') {
      steps {
        sh 'mvn -DskipTests -Ddockerfile.skip=false package dockerfile:push'
      }
    }
    stage('Deploy latest') {
      steps {
        sh 'mvn -DskipTests -Ddockerfile.skip=false -Ddockerfile.tag=latest package dockerfile:push'
      }
    }
    stage('Site') {
      steps {
        sh 'mvn site-deploy'
      }
    }
  }
}