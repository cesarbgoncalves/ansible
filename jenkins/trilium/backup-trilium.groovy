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
        // PATH="${PATH}:${WORKSPACE}/trilium-py/venv/bin"
    }

    parameters {
        booleanParam(name: 'Gerar_Backup', defaultValue: false)
        booleanParam(name: 'Enviar_AWS', defaultValue: false)
        booleanParam(name: 'K3S', defaultValue: false)
        booleanParam(name: 'OPNSENSE', defaultValue: false)
        booleanParam(name: 'NGINX_MANAGER', defaultValue: false)
        booleanParam(name: 'MYSQL_PIHOLE', defaultValue: false)
    }

    stages {
        stage('Validando os repositórios') {
            steps {
                script {
                    sh """
                    python3 -m venv venv
                    source ./venv/bin/activate
                    pip install trilium-py
                    which python
                    set
                    // python3 scripts-trilium/backup.py
                    """
                }
            }
        }
        // stage('Validando os repositórios') {
        //     steps {
        //         script {
        //             sh """
        //             set
        //             source ${WORKSPACE}/trilium-py/venv/bin/activate
        //             pwd
        //             whereis python
        //             python -m pip3 install -r trilium-py/requirements.txt
        //             python trilium-py/backup.py
        //             """
        //         }
        //     }
        // }
        
        stage('Gerando Backup') {
            when {
                expression { params.Gerar_Backup }
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
                script {
                        script {
                            sh buildCommand(playbook: "playbooks/trilium/enviar-backup.yaml")
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
    if (params.K3S) { targetList.push("k3s") }
    if (params.OPNSENSE) { targetList.push("opnsense") }
    if (params.NGINX_MANAGER) { targetList.push("nginx_manager") }
    if (params.MYSQL_PIHOLE) { targetList.push("mysql") }

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