from trilium_py.client import ETAPI
import datetime
server_url = 'https://trilium.cesarbgoncalves.com.br'
token = 't2qG3S5qW8kE_aJ/BYfubWL7vN8251cwMEgRFrCrgQsmj32zORZ4m2wY='
ea = ETAPI(server_url, token)


ea.move_yesterday_unfinished_todo_to_today()
