package br.edu.ifpr.mcp_notes.client;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/*
 * Console menu: lista as tools do server, pede os args
 * (do JSON) e mostra o resultado.
 */
public class ConsoleMenu {
   private final McpSyncClient      client;
   private final Scanner            scanner;
   private final PrintStream        out;
   private final ToolArgumentReader argumentReader;

   public ConsoleMenu(McpSyncClient client, Scanner scanner, PrintStream out) {
      this.client         = client;
      this.scanner        = scanner;
      this.out            = out;
      this.argumentReader = new ToolArgumentReader(scanner, out);
   }

   public void run() {
      List<Tool> tools = client.listTools().tools();

      while (true) {
         out.println();
         out.println("========== CLIENT ==========");
         for (int i = 0; i < tools.size(); i++)
            out.printf("%d - %s: %s%n", i + 1,tools.get(i).name(),
                                        tools.get(i).description());
         out.println("0 - Sair");
         out.print("Escolha uma tool: ");
         out.flush();

         if (!scanner.hasNextLine())
            return;

         String input = scanner.nextLine().trim();

         if (input.equals("0"))
            return;

         Optional<Tool> tool = select(tools,input);

         if (tool.isPresent())
            runTool(tool.get());
         else
            out.println("Tool nao encontrada");
      }
   }

   private void runTool(Tool tool) {
      Map<String,Object> arguments = argumentReader.read(tool.inputSchema());

      try {
         show(client.callTool(CallToolRequest.builder(tool.name())
                                             .arguments(arguments)
                                             .build()));
      }
      catch (Exception e) {
         out.println("Falhou a call: " + e.getMessage());
      }
   }

   private void show(CallToolResult result) {
      out.println();
      out.println("Erro: " + (Boolean.TRUE.equals(result.isError()) ? "sim"
                                                                      : "nao"));
      for (var content : result.content())
         out.println(content instanceof TextContent text ? text.text()
                                                         : content.toString());
   }

   // Acha uma tool pelo nome ou numero
   private Optional<Tool> select(List<Tool> tools, String input) {
      try {
         int index = Integer.parseInt(input) - 1;

         if (index >= 0 && index < tools.size())
            return Optional.of(tools.get(index));
      }
      catch (NumberFormatException ignored) {
         // nao é um numero, tenta um nome
      }
      return tools.stream().filter(t -> t.name().equalsIgnoreCase(input))
                  .findFirst();
   }
}
