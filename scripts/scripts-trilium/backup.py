from trilium_py.client import ETAPI
import datetime
server_url = 'https://trilium.cesarbgoncalves.com.br'
token = 'J1NjumuL6X1D_V6OFjfUsbFRhjMVRQhYqZK1uE3c55cZ5MTYX6N3568k='
ea = ETAPI(server_url, token)

agora = datetime.datetime.now()

data_formatada = agora.strftime("%Y-%m-%d")

res = ea.backup(data_formatada)
