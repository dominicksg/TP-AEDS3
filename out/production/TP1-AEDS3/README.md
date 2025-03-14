#  :pushpin:Checklist Obrigatório

- O CRUD (com índice direto) de categorias foi implementado? **SIM** :white_check_mark:
- Há um índice indireto de nomes para as categorias? **SIM** :white_check_mark:
- O atributo de ID de categoria, como chave estrangeira, foi criado na classe Tarefa? **SIM** :white_check_mark:
- Há uma árvore B+ que registre o relacionamento 1:N entre tarefas e categorias? **SIM** :white_check_mark:
- É possível listar as tarefas de uma categoria? **SIM** :white_check_mark:
- A remoção de categorias checa se há alguma tarefa vinculada a ela? **SIM** :white_check_mark:
- A inclusão de categoria em uma tarefa se limita às categorias existentes? **SIM** :white_check_mark:
- O trabalho está funcionando corretamente? **SIM** :white_check_mark:
- O trabaho está completo? **SIM** :white_check_mark:
- O trabalho é original e não é cópia de um trabalho de outro grupo? **SIM** :white_check_mark:

---

# :pushpin:Sobre o Trabalho

O objetivo deste trabalho é implementar um sistema de gerenciamento de tarefas que utilize uma tabela hash extensível para o índice direto e uma árvore B+ para indexação do relacionamento 1:N entre tarefas e categorias. A classe de categorias também contará com índice direto e indireto, sendo este último baseado em seu nome. O sistema permitirá operações de inserção, busca, atualização e exclusão de registros para todas as entidades, garantindo armazenamento e recuperação eficientes na memória secundária por meio de um arquivo binário. O tratamento adequado de remoções lógicas e alterações de tamanho dos registros também será implementado para assegurar a integridade dos dados.

---

# :pushpin:Estruturação Geral

## Estrutura de Dados na Classe Tarefa

- **id (INT)**: Identificador único para cada instância da classe.
- **Nome (STRING)**: Nome descritivo associado à instância.
- **Data de Criação (LOCALDATE)**: Data em que a instância foi criada.
- **Data de Conclusão (LOCALDATE)**: Data em que a instância foi concluída.
- **Status (TIPO 'STATUS')**: Indicador textual do estado atual da instância (por exemplo, "pendente", "progresso", "concluído").
- **Prioridade (BYTE)**: Nível de prioridade da instância, representado como um valor numérico.
- **id Categoria (INT)**: Chave estrangeira que referencia o id da categoria à qual a tarefa pertence.

## Estrutura do Registro de Tarefa

Cada objeto será representado como um vetor de bytes, preparado para ser armazenado na memória secundária (em forma de arquivo). A conversão dos registros em bytes será realizada usando as classes `ByteArrayInputStream`, `ByteArrayOutputStream`, `DataInputStream` e `DataOutputStream`, que facilitam a leitura e escrita dos dados no formato binário adequado. A estrutura do vetor de bytes será definida da seguinte forma:

- **id** = (INT)
- **Nome** = (STRING UTF-8)
- **Data de Criação** = (INT)
- **Data de Conclusão** = (INT)
- **Status** = (STRING UTF-8)
- **Prioridade** = (BYTE)
- **id Categoria** = (INT)

## Estrutura do Arquivo

O arquivo de armazenamento conterá um cabeçalho e uma sequência de registros. A estrutura do arquivo será organizada da seguinte forma:

- **Cabeçalho**:
  - Último ID Registrado: Um inteiro que armazena o último ID utilizado até o momento.

- **Registros**:
  - Lápide (BYTE): Indicador de remoção lógica, onde 0 significa ativo e 1 significa removido.
  - Tamanho do Registro (SHORT): Um valor de 2 bytes que indica o tamanho do registro em bytes.
  - Registro (Vetor de Bytes): A representação binária do objeto, conforme descrito na estrutura de registro.

[Último ID] -> [Lápide 1] -> [Tamanho do Registro 1] -> [Registro 1 em Bytes] -> [Lápide 2] -> [Tamanho do Registro 2] -> [Registro 2 em Bytes] -> ... -> EOF


---

# :pushpin:Classes Criadas e seus Métodos

## Classe Arquivo

A classe `Arquivo<T>` gerencia registros genéricos que implementam a interface `Registro`, realizando operações de CRUD (criar, ler, atualizar e deletar) em um arquivo de bytes. Ela utiliza a tabela hash extensível (`HashExtensivel<ParEnderecoId>`) para armazenar índices diretos.

### Construtor

- **Arquivo(String na, Constructor<T> c)**: Inicializa o arquivo de dados e o índice hash extensível. Se o arquivo não existir, cria o arquivo e o cabeçalho.

### Métodos

- `int create(T obj)`: Cria um novo registro no arquivo, atribui um novo ID, armazena o registro no final do arquivo, e insere a referência no índice hash.
- `T read(int id)`: Lê um registro a partir de seu ID, utilizando o índice hash para localizar o endereço no arquivo.
- `boolean delete(int id)`: Marca um registro como excluído (usando uma lápide) e remove sua entrada do índice hash.
- `boolean update(T novoObj)`: Atualiza um registro existente. Se o novo registro for maior que o anterior, move-o para o final do arquivo e ajusta o índice hash.
- `void close()`: Fecha o arquivo de dados e o índice hash associado.

---

## Classe Tarefas

A classe `Tarefas` implementa a interface `Registro` e representa uma tarefa com informações como o nome, a data de criação, a data de conclusão (se houver), e seu status (usando o enum `Status`).

### Construtores

- **Tarefas(int id, String nome, LocalDate createdAt)**: Inicializa uma tarefa com um ID, nome e data de criação definidos. O status inicial é PENDENTE e a data de conclusão (doneAt) é null.
- **Tarefas()**: Inicializa uma tarefa com valores padrão: ID 0, nome vazio, a data de criação como a data atual, status PENDENTE, e doneAt como null.

### Métodos

- `void setStatus(Status status)`: Define o status da tarefa. O status é um valor do enum `Status`, que pode ser PENDENTE, PROGRESSO ou CONCLUIDO.
- `void setDoneAt(LocalDate doneAt)`: Define a data de conclusão (doneAt) da tarefa. Usado apenas quando a tarefa é marcada como CONCLUIDO.
- `void setIdCategoria(int idCategoria)`: Define o id da categoria a qual a tarefa pertence.
- `String getNomeCategoria(int id)`: Realiza uma busca no arquivo de categorias com base no id e retorna o nome da categoria em questão.

### Serialização e Deserialização

- `byte[] toByteArray()`: Converte o objeto `Tarefas` em um array de bytes para armazenamento. Isso inclui o ID, nome, data de criação (em dias desde o epoch), status, id da categoria e, se disponível, a data de conclusão (doneAt). Se a data de conclusão for null, o método grava um valor booleano false para indicar que doneAt não existe.
- `void fromByteArray(byte[] b)`: Reconstrói o objeto `Tarefas` a partir de um array de bytes. Lê os dados armazenados, incluindo o ID, nome, data de criação, status, id de categoria e, se aplicável, a data de conclusão.

### Relacionamento com Status

A enumeração `Status` define três possíveis estados para uma tarefa:

- **PENDENTE**: A tarefa ainda não foi iniciada.
- **PROGRESSO**: A tarefa está em andamento.
- **CONCLUIDO**: A tarefa foi finalizada. Quando uma tarefa é marcada como CONCLUIDO, a data de conclusão (doneAt) deve ser definida.

Esses valores são usados pela classe `Tarefas` para indicar em qual fase a tarefa se encontra e impactam a maneira como a tarefa é exibida ou manipulada.

## Classe Categoria

A classe `Categoria` implementa a interface `Registro` e representa uma categoria com informações de Nome e ID.

### Construtores

- **Categoria(int i, String n)**: Inicializa uma categoria com os valores de id e nome determinados por parametro.
- **Categoria(String n)**: Inicializa uma categoria com ID padrão "-1" e define o nome com base no valor passado por paramentro.
- **Categoria()**: Inicializa uma categoria com valores padrão: id = -1, nome = "".

### Métodos

- `void setId(int id)`: Determina o id da categoria.
- `int getId()`: Recupera o id da categoria.
- `void setNome(String nome)`: Determina o nome da categoria.
- `String getNome()`: Recupera o Nome da tarefa.
- `String toString()`: Retorna o objeto categoria como uma string.

### Serialização e Deserialização

- `byte[] toByteArray()`: Converte o objeto `Categotia` em um array de bytes para armazenamento. Isso inclui o ID e nome do objeto.
- `void fromByteArray(byte[] b)`: Reconstrói o objeto `Categoria` a partir de um array de bytes. Lê os dados armazenados, incluindo o ID e o nome.

**OBS**: Os métodos e classes que não foram abordados aqui são autoexplicativos ou seguem o mesmo padrão apresentado em sala de aula, o que acreditamos tornar redundantes suas explicações.

---

# :pushpin:Opinião do Grupo sobre o Desenvolvimento

Dividimos as tarefas deste trabalho de forma democrática entre os quatro integrantes.

:white_check_mark:Desenvolvimento do programa, Organização e Implementação dos indices e classes - Victor, Douglas  
:white_check_mark:Arquitetura do projeto, Testes e Documentação - André, João  

A parte mais desafiadora do projeto foi integrar as entidades tarefa e categoria, mantendo o relacionamento 1:N por meio da árvore B+ e implementando todas as restrições definidas para essas entidades. Durante uma das fases de teste, identificamos um problema ao atualizar a categoria de uma tarefa: mesmo após a atualização, a tarefa permanecia vinculada à categoria anterior. No entanto, conseguimos identificar a causa do erro e corrigi-lo.
Todos os requisitos foram implementados com sucesso. Como já havíamos trabalhado juntos anteriormente, a colaboração foi eficaz e todos aprenderam em conjunto. Embora alguns problemas tenham surgido, a maior parte do código foi discutida em sala de aula, o que facilitou a implementação. No entanto, sentimos um aumento na complexidade em relação ao primeiro trabalho, especialmente devido ao número maior de classes, estruturas de armazenamento e restrições que precisaram ser aplicadas aos processos do sistema

# :pushpin:Integrantes

- **André Luiz Rocha Cabral**
- **Douglas Nicolas Silva Gomes**
- **joão Paulo Dias Estevão**
- **Victor Sousa Lima**
