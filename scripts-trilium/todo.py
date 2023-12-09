from trilium_py.client import ETAPI
import datetime
server_url = 'https://trilium.cesarbgoncalves.com.br'
token = 't2qG3S5qW8kE_aJ/BYfubWL7vN8251cwMEgRFrCrgQsmj32zORZ4m2wY='
ea = ETAPI(server_url, token)

## Criando Todo
todo = input("Digite o próximo Todo: ")
ea.add_todo(todo)