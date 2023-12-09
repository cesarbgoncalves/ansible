
from trilium_py.client import ETAPI
import datetime
server_url = 'https://trilium.cesarbgoncalves.com.br'
token = 'J1NjumuL6X1D_V6OFjfUsbFRhjMVRQhYqZK1uE3c55cZ5MTYX6N3568k='
ea = ETAPI(server_url, token)

# Criando notas em INBOX
agora = datetime.datetime.now()

data_formatada = agora.strftime("%d/%m/%Y %H:%M:%S") 

data_formatadaID = agora.strftime("%Y-%m-%d")

res = res = ea.backup(data_formatadaID)



