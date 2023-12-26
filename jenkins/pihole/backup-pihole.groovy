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
                        script {
                            sh buildCommand(playbook: "playbooks/pihole/enviar-backup.yaml")
                        }
                }
            }
        }
    }
    post {
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
    if (params.Gerar_Backup) { targetList.push("mysql") }

    def limit = targetList.join(',')
    if (limit) limit = /--limit '${limit.toLowerCase()}'/
    return limit
}

def buildCommand(Map map = [:]) {
    def callback = map.useCallback ? "ANSIBLE_STDOUT_CALLBACK=diy" : ""
    def verbose = (map.verbose != null ? map.verbose : true) ? "--verbose" : ""
    def list_hosts = (map.list_hosts != null ? map.list_hosts : false) ? "--list-hosts" : ""
    def limit = getLimit(map)

    return """
        $callback ansible-playbook $verbose ${map.playbook} $list_hosts \
        -i hosts/proxmox.yaml $limit \
        --user=$SSH_CREDENTIAL_USR --private-key=$SSH_CREDENTIAL \
        -e base_path=\$PWD
    """
}