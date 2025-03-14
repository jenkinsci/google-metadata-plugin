/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/
*/
buildPlugin(
  forkCount: '1C',
  useContainerAgent: true,
  configurations: [
    [platform: 'linux', jdk: 21],
    [platform: 'windows', jdk: 17],
  ]
)

pipeline {
    agent any

    environment {
        GCS_BUCKET = 'your-bucket-name'  // Replace with your actual GCS bucket name
        GCS_CREDENTIALS_ID = 'your-jenkins-credentials-id' // Set in Jenkins credentials
    }

    stages {
        stage('Upload to GCS') {
            steps {
                script {
                    googleStorageUpload(
                        credentialsId: GCS_CREDENTIALS_ID,
                        bucket: GCS_BUCKET,
                        pattern: '**/*.txt',  // Upload all text files
                        sharedPublicly: false, // Set true if public access is needed
                        metadata: [
                            "Content-Type": "text/plain",
                            "Content-Encoding": "gzip"
                        ]
                    )
                }
            }
        }
    }
}
