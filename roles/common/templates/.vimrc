"Instalacao de Plugins:
call plug#begin('~/.vim/plugged')
Plug 'dracula/vim',{'as':'dracula'}
Plug 'preservim/nerdtree'
Plug 'vim-airline/vim-airline'
Plug 'tpope/vim-surround'
Plug 'jiangmiao/auto-pairs'
call plug#end()
"Instalacao de Plugins



" Exit Vim if NERDTree is the only window remaining in the only tab.
"     autocmd BufEnter * if tabpagenr('$') == 1 && winnr('$') == 1 && exists('b:NERDTree') && b:NERDTree.isTabTree() | quit | endif
" Se abrir o vim sem chamar nenhum arquivo, abre o NERDTree
autocmd StdinReadPre * let s:std_in=1
autocmd VimEnter * if argc() == 0 && !exists("s:std_in") | NERDTree | endif

colorscheme dracula
set encoding=utf-8
map q :quit<CR>
map <C-s> :write<CR>


set syntax=on               " modo visual/colorido ativo
set number                  " ativa a numeração das linhas
set showmode                " apresenta o modo de utilização atual (command/insert)
set ignorecase              " ignora case sensitive durante a busca
set ruler                   " apresenta a posição do cursor
set hlsearch                " destaca os itens pesquisados
set showcmd                 " visualiza comandos incompletos
set smarttab                " trabalha a identação do arquivo
set nowrap                  " desativa a quebra de linha
set sm                      " ativar/desativar as coincidencias
set laststatus=1            " exibe a linha de status
set title                   " habilita o titulo
set term=xterm-256color     " modo do terminal
set smartcase               " modo de pesquisa
set incsearch               " busca incremental
set autoindent              " auto identação
set shiftwidth=2            " tabulação
set smartindent             " identação
set softtabstop=2           " tabulação
set undolevels=1000         " número máximo de restore 
set cursorcolumn            " mostra foco na coluna do cursor
set cursorline		    " mostra foco na linha do cursor
set relativenumber	    " Mostra as linhas com base onde está o cursor
set wildmenu		    " Mostra o menu suspenso
set confirm		    
set backspace=indent,eol,start
set splitright		    " Abri arquivos ao lado do arquivo atual no NERDTree
