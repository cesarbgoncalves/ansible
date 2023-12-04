pipeline {
    agent{
        label 'jenkins-slave-atl'
    }

    options {
        disableConcurrentBuilds()
        preserveStashes(buildCount: 10)
        ansiColor('xterm')
    }

    environment {
        PYTHONUNBUFFERED="1"
        ANSIBLE_COLOR_CHANGED="blue"
        SSH_CREDENTIAL=credentials('ssh-root')
        ANSIBLE_CONFIG="${pwd}/ansible.cfg"
        TARGET_LIST = 'all'
        PATH = "${PATH}:/usr/local/bin/ansible"
    }

    parameters {
        booleanParam(name: 'K3S', defaultValue: false)
        booleanParam(name: 'OPNSENSE', defaultValue: false)
        booleanParam(name: 'NGINX_MANAGER', defaultValue: false)
        booleanParam(name: 'MYSQL_PIHOLE', defaultValue: false)
    }

    stages {
        stage('validações') {
            steps {
                script {
                    sh(script: """
                        ls -ltrh
                        pwd
                    """)
                }
            }
        }
        stage('ping') {
            steps {
                ansiColor('xterm') {
                ansiblePlaybook(
                playbook: 'playbooks/testes/ping.yaml',
                inventory: 'hosts/proxmox.yaml',
                credentialsId: 'ssh-root',
                colorized: true)
                }
            }
        }
    }
}

// def getLimit(Map map = [:]) {
//     def targetList = []
//     if (params.k3s) { targetList.push("k3s") }
//     if (params.OPNSENSE) { targetList.push("opnsense") }
//     if (params.NGINX_MANAGER) { targetList.push("nginx_manager") }
//     if (params.MYSQL_PIHOLE) { targetList.push("mysql") }

//     def limit = targetList.join(',')
//     if (limit) limit = /--limit '${limit.toLowerCase()}'/

//     return limit
// }

// def buildCommand(Map map = [:]) {
//     def callback = map.useCallback ? "ANSIBLE_STDOUT_CALLBACK=diy" : ""
//     def tags = map.tags ? map.tags.collect{ "--tags $it" }.join(" ") : ""
//     def extra = map.extra ? map.extra.collect{entry -> "--extra-vars '${entry.key}=${entry.value}'"}.join(" ") : ""
//     def verbose = (map.verbose != null ? map.verbose : true) ? "--verbose" : ""
//     def list_hosts = (map.list_hosts != null ? map.list_hosts : false) ? "--list-hosts" : ""

//     col = COLUNA.split(',').first().replaceAll(/(\w+)(\d+)/,/$1 $2/).toUpperCase()
//     def limit = getLimit(map)

//     return """
//         $callback ansible-playbook $verbose ${map.playbook} $list_hosts $tags \
//         -i hosts/gt.${ENV}.yml -i hosts/tb.${ENV}.yml $limit $extra \
//         --user=$SSH_CREDENTIAL_USR --private-key=$SSH_CREDENTIAL \
//         -e 'COLUMN="$col"' \
//         -e base_path=\$PWD
//     """
// }
