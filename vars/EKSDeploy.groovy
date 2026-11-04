def call(Map configmap) {
    pipeline { 
    agent { 
        node { 
            label 'AGENT-1' 
        } 
    } 

    environment { 
        COURSE = "jenkins" 
        APP_VERSION = configmap.get('APP_VERSION')
        PROJECT = configmap.get('PROJECT')
        COMPONENT = configmap.get('COMPONENT')
        deploy_to = configmap.get('deploy_to')
        REGION = "us-east-1"
    } 
    options {
        timeout(time: 50, unit: 'MINUTES')
        disableConcurrentBuilds()
     }
    // parameters {
    //     string(name: 'APP_VERSION', description: 'Which version you want to deploy?')
    //     choice(name: 'deploy_to', choices: ['dev', 'qa', 'prod'], description: 'Pick something')

    // }
    stages {       
        stage('depoly') { 
            steps { 
                script { 
                     withAWS(region:'us-east-1',credentials:'aws-creds') {
                        sh """
                            set -e
                            aws eks update-kubeconfig --region ${REGION} --name ${PROJECT}-${deploy_to}
                            kubectl get nodes
                            echo ${APP_VERSION}
                            echo ${deploy_to}
                            sed -i "s/IMAGE_VERSION/${appVersion}/g" values.yaml
                            helm upgrade --install ${COMPONENT} -f values-${deploy_to}.yaml -n ${PROJECT} --atomic --wait --timeout=5m .
                        """
                } 

         stage('funtional testing'){
            steps{
                script{
                    sh """
                       echo "do the functional testing"
                       """
                     }
                 }
              }
            } 
         } 
        }
    }
    
    post { 
        always {  
            echo 'I will always say Hello again!' 
            cleanWs()  
        } 

        success { 
            echo 'Run if pipeline success' 
        } 

        failure { 
            echo 'Run if pipeline failure' 
        } 
        aborted {
            echo "pipeline is aborted"
        }
    } 
}

}