pipeline {
    agent any

    triggers {
        // Poll SCM every 5 minutes
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Build and Test') {
            steps {
                echo 'Running Maven build and tests against SQLite database...'
                sh 'mvn clean test -Dspring.profiles.active=test'
            }
        }
        stage('Deploy to Web Server') {
            steps {
                echo 'Running Ansible Playbook to deploy...'
                sh 'ansible-playbook -i inventory.ini playbook.yaml'
            }
        }
    }

    post {
        failure {
            echo 'Build failed! Sending email notifications...'
            emailext (
                subject: "Build Failed: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
                body: """<p>Build Failed.</p>
                         <p>Check console output at: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>""",
                to: 'srengty@gmail.com',
                recipientProviders: [
                    [$class: 'DevelopersRecipientProvider']
                ]
            )
        }
        success {
            echo 'Build and Deployment completed successfully!'
        }
    }
}
