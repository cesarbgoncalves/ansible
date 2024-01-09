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
    }

    parameters {
        password(name: 'NOVA_SENHA', defaultValue: '', description: 'Digite a nova senha')

    }

    stages {
        stage('Configurando chaves') {
            when {
                expression { params.NOVA_SENHA }
            }
            environment {
                BW_CLIENTID = credentials('BW_CLIENTID')
                BW_CLIENTSECRET = credentials('BW_CLIENTSECRET')
                BW_PASSWORD = credentials('BW_PASSWORD')
            }
            steps {
                script {
                    sh buildCommand(playbook: "playbooks/bitwarden/troca-senha.yaml")
                    // sh(script: """
                    // bw config server https://bitwarden.cesarbgoncalves.com.br --quiet
                    // bw login --apikey
                    // bw unlock --passwordenv BW_PASSWORD
                    // bw list items --folderid '29752335-d158-4a48-b036-f206289ce954'
                    // """)
                }
            }
        }
    }
    // post {
    //     success {
    //         script {
    //             sh buildCommand(playbook: "playbooks/pihole/apaga-backup.yaml")
    //         }
    //     }
    //     always {
    //         echo 'Enviando e-mail para cesarbgoncalves@gmail.com'
    //         emailext(
    //             subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}",
    //             to: "cesarbgoncalves@gmail.com",
    //             recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']],
    //             body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}"
                
    //         )
    //     }
    // }
}

def getLimit(Map map = [:]) {
    def targetList = []
    if (params.Gerar_Backup) { targetList.push("k3s") }
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
        -e AWS_ACCESS_KEY_ID=$aws_access_key \
        -e AWS_SECRET_ACCESS_KEY=$aws_secret_key
    """
}
