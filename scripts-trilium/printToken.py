from trilium_py.client import ETAPI

server_url = 'https://trilium.cesarbgoncalves.com.br'
password = 'qzd&2mAxKa3VNh5Fm#kz'
ea = ETAPI(server_url)
token = ea.login(password)
print(token)