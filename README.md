# Simulador de Sistema de Arquivos (com Log de Operações)

Este projeto implementa um simulador de sistema de arquivos básico em Java, com funcionalidades de persistência de estado e um sistema de log para registrar as operações realizadas. É ideal para entender conceitos fundamentais de sistemas de arquivos e o uso de serialização para persistência de dados.

## 🚀 Como Começar


### Estrutura de Pastas

O workspace contém duas pastas por padrão:

* `src`: Contém todo o código-fonte Java do projeto.
* `lib`: Destina-se a manter bibliotecas e dependências externas (não usadas neste projeto atualmente).

Os arquivos `.class` compilados serão gerados na pasta `bin` por padrão.

> Se quiser personalizar a estrutura de pastas, abra `.vscode/settings.json` e atualize as configurações relacionadas lá.


## 📄 Relatório do Simulador de Sistema de Arquivos

### Metodologia

O simulador de sistema de arquivos foi desenvolvido em linguagem de programação Java. A abordagem consiste em simular as operações básicas de um sistema de arquivos como:
* Criação de arquivos e diretórios
* Exclusão de arquivos e diretórios
* Renomeação de arquivos e diretórios
* Cópia de arquivos
* Navegação entre diretórios

Todas essas funcionalidades são executadas e os resultados são exibidos no console, simulando uma interface de linha de comando (CLI). Um mecanismo de **log de operações** foi integrado para registrar cada ação.

### Parte 1: Introdução ao Sistema de Arquivos e Log de Operações

#### Descrição do Sistema de Arquivos Simulado

Um sistema de arquivos é um método e uma estrutura de dados que um sistema operacional usa para controlar como os dados são armazenados e recuperados. Sem um sistema de arquivos, as informações colocadas em um meio de armazenamento seriam um grande bloco de dados sem capacidade de distinguir um pedaço de informação do outro.

Este simulador organiza arquivos em uma estrutura hierárquica (diretórios e subdiretórios), facilita o acesso, a modificação e a exclusão de dados, e gerencia o espaço de armazenamento de forma conceitual (não no sistema de arquivos real do seu computador, mas dentro do próprio programa Java).

#### Log de Operações

O **log de operações** (implementado no arquivo `log_jornal.txt`) serve como um registro cronológico de todas as modificações que ocorrem no sistema de arquivos simulado. Ele é crucial para auditar as ações do usuário e entender o histórico das operações. Diferentemente de uma técnica de *journaling* tradicional com recuperação, este log é apenas um registro, e não é usado para refazer operações após uma falha.

### Parte 2: Arquitetura do Simulador

#### Estrutura de Dados

O simulador utiliza as seguintes classes para representar a estrutura do sistema de arquivos:

* `Pasta`: Representa um diretório. Contém uma lista de `Arquivo`s e uma lista de `Pasta`s (subdiretórios). Possui também uma referência para sua `Pasta` pai, permitindo a navegação na hierarquia.
* `Arquivo`: Representa um arquivo. Armazena seu nome e conteúdo (como uma `String`).
* `Diario`: Gerencia temporariamente os detalhes de uma operação para serem gravados no log TXT. Contém uma lista de objetos `EntradaNoDiario`. **Não é mais utilizado para fins de persistência ou recuperação do estado do sistema de arquivos.**
* `EntradaNoDiario`: Representa uma única entrada de operação. Armazena o `TipoDeOperacao` (CRIAR_ARQUIVO, DELETAR_ARQUIVO, etc.), o caminho do diretório onde a operação ocorreu, o nome antigo/novo do arquivo/pasta, e o conteúdo (se aplicável).
* `TipoDeOperacao`: Um `enum` que define os diferentes tipos de operações que podem ser registradas.

#### Persistência do Estado

O estado completo do sistema de arquivos simulado é salvo em um arquivo serializado chamado `arquivodosistema_estado.ser`. Isso garante que, ao reiniciar o simulador, a estrutura de pastas e arquivos seja restaurada exatamente como estava quando foi encerrado.

### Parte 3: Implementação em Java

* `SimuladoSistemaArquivo`: Esta é a classe principal que orquestra todo o simulador. Ela inicializa a raiz do sistema de arquivos (`root`), mantém a referência à `pastaAtual`, e utiliza o `Diario` para gerar entradas de log. Contém métodos para todas as operações de arquivo e diretório (`criarArquivo`, `deletarArquivo`, `renomearArquivo`, `copiarArquivo`, `criarPasta`, `deletarPasta`, `renomearPasta`, `mudarPasta`, `listarConteudo`), além dos métodos de persistência (`salvarEstado`, `carregarEstado`). O método `main` é responsável por iniciar o shell de comando interativo.
* Classes `Arquivo` e `Pasta`: São classes auxiliares que modelam os elementos fundamentais do sistema de arquivos. Elas são `Serializable` para permitir a persistência do estado do sistema.
* Classes `Diario` e `EntradaNoDiario`: São responsáveis por registrar os detalhes de cada operação para serem escritos no `log_jornal.txt`. A classe `Diario` atua como um container temporário para a `EntradaNoDiario` atual.

### Parte 4: Instalação e Funcionamento

Para compilar e executar o simulador:

1.  **Clone o repositório** (ou baixe os arquivos diretamente).
2.  **Navegue até a pasta raiz do projeto** no seu terminal.
3.  **Execute o simulador**:
4.  O simulador iniciará e você verá um prompt de comando (`/$`). Digite `cs` para ver a lista de comandos disponíveis.

Ao executar operações que modificam o sistema de arquivos (como `mkfile`, `mkdir`, `rmfile`, etc.), um arquivo `log_jornal.txt` será criado ou atualizado na mesma pasta onde o simulador é executado, contendo os registros de todas as operações. O estado do sistema de arquivos será salvo em `arquivodosistema_estado.ser`.
