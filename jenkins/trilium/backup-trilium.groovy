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
    }

    parameters {
        booleanParam(name: 'Gerar_Backup', defaultValue: true)
        booleanParam(name: 'Compactar', defaultValue: true)
        booleanParam(name: 'Enviar_AWS', defaultValue: true)
        booleanParam(name: 'K3S', defaultValue: true)
    }

    stages {
        stage('Criando backup Trilium') {
            when {
                expression { params.Gerar_Backup }
            }
            steps {
                script {
                    sh """
                    python3 -m venv venv
                    source ./venv/bin/activate
                    pip install trilium-py
                    python3 scripts/scripts-trilium/backup.py
                    """
                }
            }
        }
        stage('Compactando Arquivo') {
            when {
                expression { params.Compactar }
            }
            steps {
                script {
                        sh buildCommand(playbook: "playbooks/trilium/backup-trilium.yaml")
                }
            }
        }
        stage('Enviando para a AWS') {
            when {
                expression { params.Enviar_AWS }
            }
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-pessoal-cesar', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    sh buildCommand(playbook: "playbooks/trilium/enviar-backup.yaml", aws_access_key: "${AWS_ACCESS_KEY_ID}", aws_secret_key: "${AWS_SECRET_ACCESS_KEY}")
                }
            }
        }
    }
    post {
        success {
            script {
                sh buildCommand(playbook: "playbooks/trilium/apaga-backup.yaml")
            }
        }
        always {
            echo 'Enviando e-mail para cesarbgoncalves@gmail.com'
            
            emailext body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']],
                subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}"
        }
    }
}

def getLimit(Map map = [:]) {
    def targetList = []
    if (params.K3S) { targetList.push("k3s") }

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
        -e base_path=\$PWD \
        -e AWS_ACCESS_KEY_ID=$aws_access_key \
        -e AWS_SECRET_ACCESS_KEY=$aws_secret_key
    """
}