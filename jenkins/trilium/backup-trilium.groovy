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
        ANSIBLE_CONFIG="${pwd}/ansible.cfg"
        SHELL="/bin/bash"
        TZ="America/Sao_Paulo"
    }

    parameters {
        booleanParam(name: 'Gerar_Backup', defaultValue: false)
        booleanParam(name: 'Enviar_AWS', defaultValue: false)
    }

    stages {
        
        stage('Gerando Backup') {
            steps {
                script {
                    sh buildCommand(playbook: "playbooks/trilium/backup-trilium.yaml")
                }
            }
        }
        stage('Enviando para a AWS') {
            steps {
                withAWS(credentials: 'aws-pessoal-cesar', region: 'sa-east-1') {
                    sh buildCommand(playbook: "playbooks/trilium/backup-trilium.yaml")
                }
                
                }
            }
    }
}

def getLimit(Map map = [:]) {
    def targetList = []
    if (params.Gerar_Backup) { targetList.push("k3s") }
    if (params.Enviar_AWS) { targetList.push("opnsense") }

    def limit = targetList.join(',')
    if (limit) limit = /--limit '${limit.toLowerCase()}'/
    println(limit)
    return limit
}

def buildCommand(Map map = [:]) {
    def callback = map.useCallback ? "ANSIBLE_STDOUT_CALLBACK=diy" : ""
    // def tags = map.tags ? map.tags.collect{ "--tags $it" }.join(" ") : ""
    // def extra = map.extra ? map.extra.collect{entry -> "--extra-vars '${entry.key}=${entry.value}'"}.join(" ") : ""
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