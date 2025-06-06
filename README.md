# Simulador de Sistema de Arquivos (com Log de Operacoes)

Este projeto implementa um simulador de sistema de arquivos basico em Java, com funcionalidades de persistencia de estado e um sistema de log para registrar as operacoes realizadas. E ideal para entender conceitos fundamentais de sistemas de arquivos e o uso de serializacao para persistencia de dados.

### Estrutura de Pastas

O workspace contem duas pastas por padrao:

* `src`: Contem todo o codigo-fonte Java do projeto.
* `lib`: Destina-se a manter bibliotecas e dependencias externas (nao usadas neste projeto atualmente).

Os arquivos `.class` compilados serao gerados na pasta `bin` por padrao.

## 📄 Relatorio do Simulador de Sistema de Arquivos

### Metodologia

O simulador de sistema de arquivos foi desenvolvido em linguagem de programacao Java. A abordagem consiste em simular as operacoes basicas de um sistema de arquivos como:
* Criacao de arquivos e diretorios
* Exclusao de arquivos e diretorios 
* Renomeacao de arquivos e diretorios
* Copia de arquivos
* Navegacao entre diretorios

Todas essas funcionalidades sao executadas e os resultados sao exibidos no console, simulando uma interface de linha de comando (CLI). Um mecanismo de **log de operacoes** foi integrado para registrar cada acao de forma concisa e padronizada.

### Parte 1: Introducao ao Sistema de Arquivos e Log de Operacoes

#### Descricao do Sistema de Arquivos Simulado

Um sistema de arquivos e um metodo e uma estrutura de dados que um sistema operacional usa para controlar como os dados sao armazenados e recuperados. Sem um sistema de arquivos, as informacoes colocadas em um meio de armazenamento seriam um grande bloco de dados sem capacidade de distinguir um pedaco de informacao do outro.

Este simulador organiza arquivos em uma estrutura hierarquica (diretorios e subdiretorios), facilita o acesso, a modificacao e a exclusao de dados, e gerencia o espaco de armazenamento de forma conceitual (nao no sistema de arquivos real do seu computador, mas dentro do proprio programa Java).

#### Log de Operacoes

O **log de operacoes** (implementado no arquivo `log_jornal.txt`) serve como um registro cronologico de todas as modificacoes que ocorrem no sistema de arquivos simulado. Ele e crucial para auditar as acoes do usuario e entender o historico das operacoes.

A entrada do log segue o formato: `DATA/HORA - [SALVO] Operacao: <operacao> // local <local> // nome do arquivo(ou pasta) <nome>`. Ele nao e mais utilizado para refazer operacoes apos uma falha, atuando apenas como um registro.

### Parte 2: Arquitetura do Simulador

#### Estrutura de Dados

O simulador utiliza as seguintes classes para representar a estrutura do sistema de arquivos:

* `Pasta`: Representa um diretorio. Contem uma lista de `Arquivo`s e uma lista de `Pasta`s (subdiretorios). Possui tambem uma referencia para sua `Pasta` pai, permitindo a navegacao na hierarquia.
* `Arquivo`: Representa um arquivo. Armazena seu nome e conteudo (como uma `String`).
* `Diario`: Gerencia temporariamente os detalhes da operacao em forma de `String` antes de serem gravados no log TXT. **Nao e mais utilizado para fins de persistencia ou recuperacao do estado do sistema de arquivos.**

#### Persistencia do Estado

O estado completo do sistema de arquivos simulado e salvo em um arquivo serializado chamado `arquivodosistema_estado.ser`. Isso garante que, ao reiniciar o simulador, a estrutura de pastas e arquivos seja restaurada exatamente como estava quando foi encerrado.

### Parte 3: Implementacao em Java

* `SimuladoSistemaArquivo`: Esta e a classe principal que orquestra todo o simulador. Ela inicializa a raiz do sistema de arquivos (`root`), mantem a referencia a `pastaAtual`, e utiliza o `Diario` para gerar entradas de log. Contem metodos para todas as operacoes de arquivo e diretorio (`criarArquivo`, `deletarArquivo`, `renomearArquivo`, `copiarArquivo`, `criarPasta`, `deletarPasta`, `renomearPasta`, `mudarPasta`, `listarConteudo`), alem dos metodos de persistencia (`salvarEstado`, `carregarEstado`). O metodo `main` e responsavel por iniciar o shell de comando interativo.
* Classes `Arquivo` e `Pasta`: Sao classes auxiliares que modelam os elementos fundamentais do sistema de arquivos. Elas sao `Serializable` para permitir a persistencia do estado do sistema.
* Classe `Diario`: E responsavel por armazenar e gerenciar temporariamente as `String`s de log antes de serem gravadas no `log_jornal.txt`.

### Parte 4: Instalacao e Funcionamento

Para compilar e executar o simulador:

1.  **Clone o repositorio** (ou baixe os arquivos diretamente).
2.  **Navegue ate a pasta raiz do projeto** no seu terminal.
3.  **Execute o simulador**:
4.  O simulador iniciara e voce vera um prompt de comando (`/$`). Digite `cs` para ver a lista de comandos disponiveis.

Ao executar operacoes que modificam o sistema de arquivos (como `mkfile`, `mkdir`, `rmfile`, etc.), um arquivo `log_jornal.txt` sera criado ou atualizado na mesma pasta onde o simulador e executado, contendo os registros de todas as operacoes. O estado do sistema de arquivos sera salvo em `arquivodosistema_estado.ser`.


Paulo Marconi e Francisco Arthur 
