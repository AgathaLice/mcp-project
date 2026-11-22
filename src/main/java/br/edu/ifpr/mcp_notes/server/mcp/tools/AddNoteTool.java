package br.edu.ifpr.mcp_notes.server.mcp.tools;

import java.util.Map;

import br.edu.ifpr.mcp_notes.server.mcp.NoteTool;
import br.edu.ifpr.mcp_notes.server.service.NoteService;

/* Cria uma nota */
public class AddNoteTool extends NoteTool {

   public AddNoteTool(NoteService service) {
      super(service);
   }

   @Override public String name()           { return "adiciona_nota"; }
   @Override public String schemaResource() {
      return "/json-schemas/add-note.schema.json";
   }
   @Override public String description() {
      return "Cria uma nova nota com titulo e texto.";
   }

   @Override
   public String run(Map<String,Object> arguments) {
      return "Nota criada:\n" + format(service.add(
                                 requireString(arguments,"titulo"),
                                 requireString(arguments,"texto")));
   }
}
