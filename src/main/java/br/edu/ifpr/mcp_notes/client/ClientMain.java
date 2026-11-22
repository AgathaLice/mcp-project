package br.edu.ifpr.mcp_notes.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import br.edu.ifpr.mcp_notes.common.ConfigKeys;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import tools.jackson.databind.json.JsonMapper;

/*
 * Ponto de entrada do client que inicia o Jar do server como processo,
 * conecta STDIO e abre o menu.
 * O server inicia o próprio banco (ou conecta com o existente)
 * tem também a opção de dar o banco para ele usar
 */
public final class ClientMain {
   private ClientMain() {}

   public static void main(String[] args) {
      String        dbPath = args.length > 0 ? args[0] : null;
      Path          jar    = ServerJarLocator.locate();
      McpSyncClient client = createClient(jar,dbPath);

      try {
         client.initialize();
         new ConsoleMenu(client, new Scanner(System.in),System.out).run();
      }
      finally {
         client.closeGracefully();
      }
   }

   private static McpSyncClient createClient(Path serverJar, String dbPath) {
      if (!Files.isRegularFile(serverJar))
         throw new IllegalStateException("Não encontrou Jar do server: " +
                       serverJar.toAbsolutePath());
      // Caso da erro, builda com "mvn clean verify -DskipTests"

      ServerParameters.Builder params =
         ServerParameters.builder("java")
                         .args("-jar",serverJar.toAbsolutePath().toString());

      if (dbPath != null)
         params = params.addEnvVar(ConfigKeys.DB_PATH_ENV_VAR,dbPath);

      StdioClientTransport transport = new StdioClientTransport(params.build(),
                                  new JacksonMcpJsonMapper(new JsonMapper()));

      // os logs do server vão pra stderr
      transport.setStdErrorHandler(line -> System.err.println("[SERVER] " + line));

      return McpClient.sync(transport).build();
   }
}
