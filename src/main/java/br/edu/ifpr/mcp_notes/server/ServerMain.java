package br.edu.ifpr.mcp_notes.server;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_notes.common.ConfigKeys;
import br.edu.ifpr.mcp_notes.server.mcp.NoteTool;
import br.edu.ifpr.mcp_notes.server.mcp.ToolRegistrar;
import br.edu.ifpr.mcp_notes.server.mcp.tools.AddNoteTool;
import br.edu.ifpr.mcp_notes.server.mcp.tools.DeleteNoteTool;
import br.edu.ifpr.mcp_notes.server.mcp.tools.GetNoteTool;
import br.edu.ifpr.mcp_notes.server.mcp.tools.ListNotesTool;
import br.edu.ifpr.mcp_notes.server.mcp.tools.UpdateNoteTool;
import br.edu.ifpr.mcp_notes.server.repository.SqliteNoteRepository;
import br.edu.ifpr.mcp_notes.server.service.NoteService;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import tools.jackson.databind.json.JsonMapper;

/**
 * Ponto de entrada e root: unico lugar que inicia os objetos
 * e conecta eles (constructor injection, sem framework).
 *
 * IMPORTANTE: com o STDIO transport, o standard output carrega o protocolo.
 * Nunca imprimir no System.out aqui; os logs vão pro System.err (olha o
 * logback.xml). Se imprimir no stdout o client e o server quebram.
 */
public final class ServerMain {
   private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

   private ServerMain() {}

   public static void main(String[] args) {
      Path        database = databasePath();
      NoteService service  = new NoteService(
                                    new SqliteNoteRepository(database,true));

      LOG.info("Banco de dados Notas: {}",database);

      SyncSpecification<SingleSessionSyncSpecification> mcp =
         McpServer.sync(new StdioServerTransportProvider(
                                    new JacksonMcpJsonMapper(new JsonMapper())))
                  .serverInfo(Implementation.builder("mcp-notes-server",
                                                     "0.0.1").build())
                  .capabilities(ServerCapabilities.builder().tools(true).build());

      List<NoteTool> tools     = List.of(new AddNoteTool(service),
                                         new GetNoteTool(service),
                                         new ListNotesTool(service),
                                         new UpdateNoteTool(service),
                                         new DeleteNoteTool(service));
      ToolRegistrar  registrar = new ToolRegistrar();

      tools.forEach(tool -> registrar.register(mcp,tool));
      // inicia o STDIO transport; o JVM continua enquanto ele roda
      mcp.build();
   }

   private static Path databasePath() {
      String path = System.getProperty(ConfigKeys.DB_PATH_PROPERTY);

      if (path == null || path.isBlank())
         path = System.getenv(ConfigKeys.DB_PATH_ENV_VAR);

      if (path == null || path.isBlank())
         return Path.of(System.getProperty("user.home"),".mcp-notes","notes.db");

      return Path.of(path.trim());
   }
}
