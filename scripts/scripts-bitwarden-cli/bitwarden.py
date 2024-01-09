import subprocess
import json
import sys

# Recebe os parâmetros da linha de comando
BW_CLIENTID = sys.argv[1]
BW_CLIENTSECRET = sys.argv[2]
BW_PASSWORD = sys.argv[3]
NOVA_SENHA = sys.argv[4]

# Configuração do servidor Bitwarden
subprocess.run(["bw", "config", "server", "https://bitwarden.cesarbgoncalves.com.br"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

# Login no Bitwarden usando a API Key
subprocess.run(["bw", "login", "--apikey"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

# Desbloqueio usando a senha
subprocess.run(["bw", "unlock", "--passwordenv", BW_PASSWORD], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

# Obtenção da lista de itens
result = subprocess.run(["bw", "list", "items", "--session", subprocess.check_output(["bw", "unlock", "--passwordenv", BW_PASSWORD]).decode().strip(), "--folderid", "29752335-d158-4a48-b036-f206289ce954"], capture_output=True)
lista = json.loads(result.stdout.decode().strip())

# Atualização das senhas
for item in lista:
    subprocess.run(["bw", "get", "item", item["id"], "--session", subprocess.check_output(["bw", "unlock", "--passwordenv", BW_PASSWORD]).decode().strip()], stdout=subprocess.PIPE)
    subprocess.run(["bw", "encode"], input=json.dumps({"login": {"password": NOVA_SENHA}}), stdout=subprocess.PIPE)
    subprocess.run(["bw", "edit", "item", item["id"]])

# Sincronização
subprocess.run(["bw", "sync"])
