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
        BW_CLIENTID = credentials('BW_CLIENTID')
        BW_CLIENTSECRET = credentials('BW_CLIENTSECRET')
        BW_PASSWORD = credentials('BW_PASSWORD')
    }

    parameters {
        password(name: 'NOVA_SENHA', defaultValue: 'mudar_a_senha', description: 'Digite a nova senha')

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
                }
            }
        }
    }
}

def getLimit(Map map = [:]) {
    def targetList = []
    if (params.NOVA_SENHA) { targetList.push("k3s") }
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
        -e AWS_ACCESS_KEY_ID=$aws_access_key \
        -e AWS_SECRET_ACCESS_KEY=$aws_secret_key \
        -e BW_CLIENTID = $BW_CLIENTID \
        -e BW_CLIENTSECRET = $BW_CLIENTSECRET \
        -e BW_PASSWORD = $BW_PASSWORD
    """
}
