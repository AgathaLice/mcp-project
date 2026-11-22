package br.edu.ifpr.mcp_notes.server.mcp.tools;

import java.util.Map;

import br.edu.ifpr.mcp_notes.server.mcp.NoteTool;
import br.edu.ifpr.mcp_notes.server.service.NoteService;

/* Pega uma nota por ID. */
public class GetNoteTool extends NoteTool {

   public GetNoteTool(NoteService service) {
      super(service);
   }

   @Override public String name()           { return "pega_nota"; }
   @Override public String description()    { return "Retorna a nota com o ID dado."; }
   @Override public String schemaResource() {
      return "/json-schemas/get-note.schema.json";
   }

   @Override
   public String run(Map<String,Object> arguments) {
      return format(service.get(requireLong(arguments,"id")));
   }
}
