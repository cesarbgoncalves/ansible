pipeline {
    agent{
        label 'jenkins-slave-atl'
    }

    options {
        disableConcurrentBuilds()
        preserveStashes(buildCount: 10)
    }

    environment {
        PYTHONUNBUFFERED="1"
        ANSIBLE_COLOR_CHANGED="blue"
        SSH_CREDENTIAL=credentials('ssh-root')
        ANSIBLE_CONFIG="${WORKSPACE}/ansible.cfg"
        SHELL="/bin/bash"
        TZ="America/Sao_Paulo"
        AWS_CONFIG_FILE="/home/jenkins/.aws/config"
        AWS_DEFAULT_PROFILE="cesar"
        CUSTOM_ACCESS_KEY_ID=credentials('jenkins-aws-secret-key-id')
        CUSTOM_SECRET_ACCESS_KEY=credentials('jenkins-aws-secret-access-key')
    }

    parameters {
        booleanParam(name: 'Gerar_Backup', defaultValue: true)
        booleanParam(name: 'Enviar_AWS', defaultValue: false)
    }

    stages {
        stage('Gerando Backup') {
            when {
                expression { params.Gerar_Backup }
            }
            steps {
                script {
                    sh buildCommand(playbook: "playbooks/pihole/backup-pihole.yaml")
                }
            }
        }
        stage('Enviando para a AWS') {
            when {
                expression { params.Enviar_AWS }
            }
            steps {
                script {
                    withCredentials([string(credentialsId: 'aws-pessoal-cesar', variable: 'aws_credentials')]) {
                        def creds = readJSON text: aws_credentials
                        CUSTOM_ACCESS_KEY_ID = creds['accessKeyId']
                        CUSTOM_SECRET_ACCESS_KEY = creds['secretAccessKey']
                        sh buildCommand(playbook: "playbooks/pihole/enviar-backup.yaml", aws_access_key: "$CUSTOM_ACCESS_KEY_ID", aws_secret_key: "$CUSTOM_SECRET_ACCESS_KEY" )
                    }
                }
            }
        }
    }
    post {
        always {
            echo 'Enviando e-mail para cesarbgoncalves@gmail.com'
            emailext(
                subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}",
                to: "cesarbgoncalves@gmail.com",
                recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']],
                body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}"
                
            )
        }
    }
}

def getLimit(Map map = [:]) {
    def targetList = []
    if (params.Gerar_Backup) { targetList.push("mysql") }
    def limit = targetList.join(',')
    if (limit) limit = /--limit '${limit.toLowerCase()}'/
    return limit
}

def buildCommand(Map map = [:]) {
    def callback = map.useCallback ? "ANSIBLE_STDOUT_CALLBACK=diy" : ""
    def verbose = (map.verbose != null ? map.verbose : true) ? "--verbose" : ""
    def list_hosts = (map.list_hosts != null ? map.list_hosts : false) ? "--list-hosts" : ""
    def aws_access_key = (map.aws_access_key)
    def aws_secret_key = (map.aws_secret_key)
    def limit = getLimit(map)

    return """
        $callback ansible-playbook $verbose ${map.playbook} $list_hosts \
        -i hosts/proxmox.yaml $limit \
        --user=$SSH_CREDENTIAL_USR --private-key=$SSH_CREDENTIAL \
        -e base_path=\$ \
        -e aws_access_key=$aws_access_key \
        -e aws_secret_key=$aws_secret_key
    """
}