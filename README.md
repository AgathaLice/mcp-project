# MCP Notes (client + server)

Um projeto Maven só que gera **duas aplicações**:

| Aplicação | Classe main | Artefato |
|---|---|---|
| **Server** MCP (notas guardadas no SQLite) | `br.edu.ifpr.mcp_notes.server.ServerMain` | `target/dist/mcp-notes-server.jar` |
| **Client** MCP (menu no console) | `br.edu.ifpr.mcp_notes.client.ClientMain` | `target/dist/mcp-notes-client.jar` |

As duas usam **MCP sobre STDIO**: o client inicia o server como um processo filho, e o
Claude Desktop / MCP Inspector fazem a mesma coisa. Tecnologias: Java, Apache Maven, o
MCP Java SDK oficial, JDBC puro + SQLite, JUnit 5. Sem Lombok, sem Spring, sem nenhuma
lib que gera código, sem lib de mock (os testes usam uma fake escrita na mão).
Tudo por medo de encaixar na porção de não existir lib de criação de código.

## 1. Requisitos

* JDK 21 ou mais novo (o `java.release` no `pom.xml` é 21).
* Maven.
* Node.js só pro MCP Inspector.

Eclipse: `File > Import > Maven > Existing Maven Projects`, escolhe essa pasta. Depois de
editar o `pom.xml`: botão direito no projeto > `Maven > Update Project` (Alt+F5).

## 2. Builds

TODO Verificar se a tabela está funcionando em outro PC
| Goal | O que faz |
|---|---|
| `mvn clean test` | Só os testes unitários. |
| `mvn clean verify` | Testes unitários, os dois JARs, e depois os testes de integração. |
| `mvn clean verify -DskipITs` | Testes unitários e os dois JARs, sem os de integração. |
| `mvn clean verify -Dskip.unit.tests=true` | Os dois JARs e os testes de integração, sem os unitários. |
| `mvn clean package -DskipTests` | Os dois JARs, sem nenhum teste. |
Complexo de entender

Saída: `target/dist/mcp-notes-server.jar`, `target/dist/mcp-notes-client.jar` e
`target/dist/libs/` (as dependências dos dois; mantém tudo junto).

Os testes de integração iniciam o JAR do server, então rodam depois do package (`verify`, não `test`).

## 3. Rodando

**Client** (inicia o server sozinho):

```
java -jar target\dist\mcp-notes-client.jar
java -jar target\dist\mcp-notes-client.jar C:\temp\minhas-notas.db     (arquivo de banco opcional)
```

Ele procura o `mcp-notes-server.jar` do lado dele (ou usa `-Dmcp.server.jar=...`).
Escolhe a tool pelo número ou pelo nome; o client pergunta os args usando o JSON Schema de
cada tool, então ele roda qualquer tool que o server tiver.

**Só o server:** `java -jar target\dist\mcp-notes-server.jar`. O standard output é o canal
do protocolo: nunca imprimir nele. Os logs vão pro standard error (`logback.xml`).

**Arquivo do banco:** `-Dmcp.db.path=...`, senão a variável de ambiente `MCP_DB_PATH`,
senão `<pasta do user>/.mcp-notes/notes.db`. Um arquivo novo recebe a tabela e 3 notas de
exemplo (só na primeira vez).

**MCP Inspector:**

```
npx @modelcontextprotocol/inspector java -jar target\dist\mcp-notes-server.jar
```

**Claude Desktop** (`claude_desktop_config.json`, uma chave nova no nível de cima; usa o seu caminho):
Apenas testei o Claude como IA que consegue interagir com o programa.

```json
"mcpServers": {
  "notes": {
    "command": "java",
    "args": ["-jar", "C:/caminho/do/mcp-notes/target/dist/mcp-notes-server.jar"]
  }
}
```
Olha o nome certinho pois eu mudei de inglês para português umas 5 vezes enquanto
programava, então os nomes ficaram meio estranhos

Builda de novo depois de cada mudança no código e reinicia o Claude Desktop / Inspector: eles rodam o JAR.

## 4. Tools

Toda tool devolve texto. Uma falha volta como **erro da tool** (`isError = true`) com uma
mensagem legível. Uma nota tem `id` (inteiro, gerado), `titulo` (1 a 100 caracteres) e
`texto` (1 a 2000 caracteres); o texto é cortado nas pontas (trim). Uma nota aparece como
`[#<id>] <titulo>`, uma quebra de linha e o texto.

| Tool | Args | Resultado |
| `adiciona_nota` | `titulo` (string, necessário), `texto` (string, necessário) | `Nota criada:` + a nota |
| `pega_nota` | `id` (inteiro, necessário) | a nota |
| `lista_notas` | nenhum | todas as notas separadas por uma linha em branco, ou `Sem notas encontradas` |
| `atualiza_nota` | `id` (inteiro, necessário), `titulo` (string, opcional), `texto` (string, opcional) | `Nota atualizada:` + a nota |
| `exclui_nota` | `id` (inteiro, necessário) | `Nota #<id> excluida.` |

O `atualiza_nota` mantém o valor de qualquer arg de texto que não for enviado.

Mensagens de erro:

| Mensagem | Quando |
| `O arg '<nome>' é necessário.` | falta um arg necessário |
| `O arg 'id' precisa ser um inteiro.` | o `id` não é um número inteiro |
| `O arg '<nome>' precisa ser uma string.` | `titulo` ou `texto` não é texto |
| `O 'titulo' nao pode estar vazio.` / `O 'texto' nao pode estar vazio.` | texto em branco |
| `O 'titulo' precisa ter no maximo 100 caracteres.` / `... 'texto' ... 2000 ...` | texto grande demais |
| `Adicione ao menos 'titulo' ou 'texto' para atualizar.` | `atualiza_nota` sem nada pra mudar |
| `Nota com o ID <id> nao foi encontrada.` | `pega_nota`, `atualiza_nota`, `exclui_nota` com um id que não existe |
| `O banco de dados falhou.` | problema no banco (os detalhes vão pro log) |
| `Erro interno inesperado.` | um bug (os detalhes vão pro log) |

## 5. Testes
Ainda estou tentando entender como eles funcionam e como
programá-los, espero que estejam funcionando para você também.
* **Testes unitários** (`*Test`): `NoteServiceTest` (regras de negócio), `NoteToolsTest` (as
  cinco tools, sucesso e todos os erros), `SqliteNoteRepositoryTest` (arquivo SQLite de
  verdade numa pasta temporária, incluindo as notas de exemplo) e os helpers do client
  (`ToolArgumentReaderTest`, `ServerJarLocatorTest`). O repository fake
  `InMemoryNoteRepository` é escrito na mão.
* **Teste de integração** (`NoteToolsIT`): inicia o JAR do server de verdade com um banco
  temporário, conecta um client MCP e roda **todas as tools**, inclusive os casos de erro.

## 6. Design

```
common/        ConfigKeys: nomes das configs divididas entre client e server
server/
  domain/      Note (record)
  repository/  NoteRepository (interface) e SqliteNoteRepository (JDBC)
  service/     NoteService: regras de negócio e validação
  exception/   AppException: o único erro esperado, a mensagem é mostrada pro user
  mcp/         NoteTool (classe base), ToolRegistrar (a única classe que usa o MCP SDK)
    tools/     uma classe por tool
  ServerMain   composition root: cria os objetos e conecta eles
client/        ClientMain, ConsoleMenu, ToolArgumentReader, ServerJarLocator
```

* As dependências apontam pra um lado só: tools > service > interface do repository. O
  `NoteService` não conhece nem MCP nem SQL (dependency inversion), por isso ele é testado
  com um repository fake.
* O `ToolRegistrar` é um adapter entre as nossas tools e o SDK, e trata os erros num lugar só.
* O client nunca fala o nome de uma tool (open/closed).

## 7. Adicionando uma tool nova

1. Cria `src/main/resources/json-schemas/<nome>.schema.json`
2. Cria uma classe em `server/mcp/tools` que extende `NoteTool` (nome, descrição, caminho do schema e `run`).
3. Adiciona ela na lista do `ServerMain`.
4. Adiciona testes unitários (no `NoteToolsTest`) e um caso no `NoteToolsIT`.
5. Documenta na seção 4.
