package br.edu.ifpr.mcp_notes.server.mcp;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_notes.server.exception.AppException;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/*
 * Adapter do {@link NoteTool} com o MCP SDK (padrão chatinho ein): registra o tool
 * e transforma o texto ou a exceção pra uma CallToolResult. o handling do erro
 * fica aqui, uma vez, para cada tool:
 *   AppException: problema esperado, a msg vai pro caller;
 *   qualquer outra RuntimeException: um bug, reportado normalmente.
 */
public class ToolRegistrar {
   private static final Logger       LOG    =
                                      LoggerFactory.getLogger(ToolRegistrar.class);
   private static final ObjectMapper MAPPER = new ObjectMapper();

   public void register(SyncSpecification<SingleSessionSyncSpecification> mcp,
                        NoteTool tool) {
      Tool sdkTool = Tool.builder(tool.name(),loadSchema(tool.schemaResource()))
                         .description(tool.description())
                         .build();

      mcp.tools(SyncToolSpecification.builder()
                   .tool(sdkTool)
                   .callHandler((exchange,request) ->
                                          call(tool,request.arguments()))
                   .build());
   }

   private CallToolResult call(NoteTool tool, Map<String,Object> arguments) {
      try {
         return result(tool.run(arguments),false);
      }
      catch (AppException e) {
         return result(e.getMessage(),true);
      }
      catch (RuntimeException e) {
         LOG.error("Erro inesperado na tool '{}'",tool.name(),e);

         return result("Erro interno inesperado.",true);
      }
   }

   private CallToolResult result(String text, boolean error) {
      return CallToolResult.builder()
                           .content(List.of(TextContent.builder(text).build()))
                           .isError(error)
                           .build();
   }

   private Map<String,Object> loadSchema(String resource) {
      try (InputStream is = ToolRegistrar.class.getResourceAsStream(resource)) {
         if (is == null)
            throw new IllegalArgumentException("JSON schema nao encontrado: " +
                                               resource);

         return MAPPER.readValue(is,new TypeReference<Map<String,Object>>(){});
      }
      catch (Exception e) {
         throw new RuntimeException("Nao foi possivel carregar o schema " + resource,e);
      }
   }
}
