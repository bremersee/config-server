pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn -Ddockerfile.skip=false clean package'
      }
    }
    stage('Test') {
      steps {
        sh 'mvn test'
      }
    }
    stage('Deploy') {
      steps {
        sh 'mvn -Ddockerfile.skip=false dockerfile:push'
      }
    }
    stage('Site') {
      steps {
        sh 'mvn site-deploy'
      }
    }
  }
}