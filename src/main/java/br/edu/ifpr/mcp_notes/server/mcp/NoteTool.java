package br.edu.ifpr.mcp_notes.server.mcp;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.edu.ifpr.mcp_notes.server.domain.Note;
import br.edu.ifpr.mcp_notes.server.exception.AppException;
import br.edu.ifpr.mcp_notes.server.service.NoteService;

/*
 * Classe base de toda tool. A tool é feita em Java puro, sem MCP SDK pra ser
 * testada com testes unitários; {@link ToolRegistrar} é a única classe que 
 * traduz para SDK. Para fazer uma nova tool: extender essa classe e adicionar
 * um coiso no JSON e registrar a tool no ServerMain.
 * {@link #run} retorna o texto para o caller e joga (throw) a exception
 * {@link AppException} pra um problema esperado específico(s).
 */
public abstract class NoteTool {
   protected final NoteService service;

   protected NoteTool(NoteService service) {
      this.service = service;
   }

   public abstract String name();

   public abstract String description();

   // Local da classpath do JSON dos args.
   public abstract String schemaResource();

   public abstract String run(Map<String,Object> arguments);

   // helpers das tools
   protected static String format(Note note) {
      return "[#" + note.id() + "] " + note.title() + "\n" + note.content();
   }

   protected static String formatAll(List<Note> notes) {
      if (notes.isEmpty())
         return "Sem notas encontradas";

      return notes.stream().map(NoteTool::format)
                  .collect(Collectors.joining("\n\n"));
   }

   protected static String requireString(Map<String,Object> arguments,
                                         String name) {
      String value = optionalString(arguments,name);

      if (value == null)
         throw new AppException("O arg '" + name + "' é necessário.");

      return value;
   }

   // @return o texto, ou null quando o arg não foi enviado
   protected static String optionalString(Map<String,Object> arguments,
                                          String name) {
      Object value = arguments == null ? null : arguments.get(name);

      if (value == null)
         return null;

      if (value instanceof String text)
         return text;

      throw new AppException("O arg '" + name + "' precisa ser uma string.");
   }

   protected static long requireLong(Map<String,Object> arguments, String name) {
      Object value = arguments == null ? null : arguments.get(name);

      if (value == null)
         throw new AppException("O arg '" + name + "' é necessário.");

      // Parsers JSON entendem 3 como 3.0, só aceita se for inteiro
      if (value instanceof Number number) {
         double d = number.doubleValue();

         if (d == Math.rint(d) && !Double.isInfinite(d))
            return number.longValue();
      }
      throw new AppException("O arg '" + name + "' precisa ser um inteiro.");
   }
}
