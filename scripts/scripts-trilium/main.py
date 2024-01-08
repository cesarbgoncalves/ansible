from trilium_py.client import ETAPI
import datetime
server_url = 'https://trilium.cesarbgoncalves.com.br'
token = 't2qG3S5qW8kE_aJ/BYfubWL7vN8251cwMEgRFrCrgQsmj32zORZ4m2wY='
ea = ETAPI(server_url, token)

''' #show app version
# print(ea.app_info())
'''

''' # Busca de notas por termos
res = ea.search_note(
    search=input("Digite um termo de busca: "),
)

for x in res['results']:
    print(x['noteId'], x['title'])
'''

# Criando notas em INBOX
# Pega data e hora
'''agora = datetime.datetime.now()
data_formatada = agora.strftime("%d/%m/%Y %H:%M:%S") 
data_formatadaID = agora.strftime("%Y%m%d%H%M%S")
res = ea.create_note(
    parentNoteId="UURd4lpAdDxi",
    title=data_formatada,
    type="text",
    content=input("Digite o conteudo de sua nota: \n"),
    noteId=data_formatadaID
)'''

## Criando Todo
todo = input("Digite o próximo Todo: ")
ea.add_todo(todo)