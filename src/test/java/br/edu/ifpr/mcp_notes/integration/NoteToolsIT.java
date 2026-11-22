package br.edu.ifpr.mcp_notes.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.edu.ifpr.mcp_notes.common.ConfigKeys;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.databind.json.JsonMapper;

/*
 * Teste de integração: inicia o Jar do server DE VERDADE (target/dist, criado
 * na fase package) num processo próprio, com um banco temporário, conecta um
 * client MCP por STDIO e roda todas as tools, inclusive os casos de erro.
 * Cada teste cria as notas que precisa, então um não depende do outro.
 */
class NoteToolsIT {
   private static final Path    SERVER_JAR =
              Path.of("target","dist","mcp-notes-server.jar").toAbsolutePath();
   private static final Pattern NOTE_ID    = Pattern.compile("\\[#(\\d+)]");

   @TempDir
   static Path tempDir;

   private static McpSyncClient client;

   @BeforeAll
   static void startServer() {
      assertTrue(Files.isRegularFile(SERVER_JAR),"Jar do server nao encontrado: " +
                 SERVER_JAR + ". Roda 'mvn clean verify' (o package vem antes).");

      ServerParameters params = ServerParameters.builder("java")
         .args("-jar",SERVER_JAR.toString())
         .addEnvVar(ConfigKeys.DB_PATH_ENV_VAR,
                    tempDir.resolve("it.db").toString())
         .build();

      client = McpClient.sync(new StdioClientTransport(params,
                          new JacksonMcpJsonMapper(new JsonMapper()))).build();
      client.initialize();
   }

   @AfterAll
   static void stopServer() {
      if (client != null)
         client.closeGracefully();
   }

   // ---- helpers -----------------------------------------------------------

   private static CallToolResult call(String tool, Map<String,Object> args) {
      return client.callTool(CallToolRequest.builder(tool).arguments(args).build());
   }

   private static String text(CallToolResult result) {
      return ((TextContent) result.content().get(0)).text();
   }

   private static boolean isError(CallToolResult result) {
      return Boolean.TRUE.equals(result.isError());
   }

   // Adiciona uma nota pelo server e devolve o id dela
   private static long addNote(String title, String content) {
      String  reply   = text(call("adiciona_nota",Map.of("titulo",title,
                                                         "texto",content)));
      Matcher matcher = NOTE_ID.matcher(reply);

      assertTrue(matcher.find(),"Nao tem id da nota em: " + reply);

      return Long.parseLong(matcher.group(1));
   }

   // ---- testes ------------------------------------------------------------

   @Test
   @DisplayName("o server mostra as cinco tools, cada uma com schema de objeto")
   void exposesTools() {
      Set<String> names = client.listTools().tools().stream()
                                .map(Tool::name).collect(Collectors.toSet());

      assertEquals(Set.of("adiciona_nota","pega_nota","lista_notas",
                          "atualiza_nota","exclui_nota"),names);
      for (Tool tool : client.listTools().tools())
         assertEquals("object",tool.inputSchema().get("type"),tool.name());
   }

   @Test
   @DisplayName("lista_notas: as notas de exemplo estao la")
   void listShowsSamples() {
      CallToolResult result = call("lista_notas",Map.of());

      assertFalse(isError(result));
      assertTrue(text(result).contains("Bem vindo"));
   }

   @Test
   @DisplayName("adiciona_nota e depois pega_nota e lista_notas: a nota foi guardada")
   void addThenRead() {
      long id = addNote("Titulo do IT","Texto do IT");

      assertTrue(text(call("pega_nota",Map.of("id",id))).contains("Texto do IT"));
      assertTrue(text(call("lista_notas",Map.of())).contains("Titulo do IT"));
   }

   @Test
   @DisplayName("adiciona_nota: um titulo em branco volta como erro")
   void addBlankTitle() {
      CallToolResult result = call("adiciona_nota",
                                   Map.of("titulo"," ","texto","c"));

      assertTrue(isError(result));
      assertEquals("O 'titulo' nao pode estar vazio.",text(result));
   }

   @Test
   @DisplayName("pega_nota: um id desconhecido volta como erro")
   void getMissing() {
      CallToolResult result = call("pega_nota",Map.of("id",987654));

      assertTrue(isError(result));
      assertEquals("Nota com o ID 987654 nao foi encontrada.",text(result));
   }

   @Test
   @DisplayName("atualiza_nota: muda o titulo e mantem o texto")
   void updateTitle() {
      long id = addNote("antes","fica");

      assertFalse(isError(call("atualiza_nota",Map.of("id",id,"titulo","depois"))));

      String note = text(call("pega_nota",Map.of("id",id)));

      assertTrue(note.contains("depois"));
      assertTrue(note.contains("fica"));
      assertFalse(note.contains("antes"));
   }

   @Test
   @DisplayName("atualiza_nota: nada pra mudar e id desconhecido dao erro")
   void updateErrors() {
      long id = addNote("t","c");

      assertTrue(isError(call("atualiza_nota",Map.of("id",id))));
      assertTrue(isError(call("atualiza_nota",
                              Map.of("id",987654,"titulo","x"))));
   }

   @Test
   @DisplayName("exclui_nota: a nota some depois")
   void deleteThenGet() {
      long id = addNote("pra excluir","c");

      CallToolResult deleted = call("exclui_nota",Map.of("id",id));

      assertFalse(isError(deleted));
      assertEquals("Nota #" + id + " excluida.",text(deleted));
      assertTrue(isError(call("pega_nota",Map.of("id",id))));
   }

   @Test
   @DisplayName("exclui_nota: um id desconhecido volta como erro")
   void deleteMissing() {
      assertTrue(isError(call("exclui_nota",Map.of("id",987654))));
   }
}
