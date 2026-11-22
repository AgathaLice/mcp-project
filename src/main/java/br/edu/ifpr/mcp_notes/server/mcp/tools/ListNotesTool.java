package br.edu.ifpr.mcp_notes.server.mcp.tools;

import java.util.Map;

import br.edu.ifpr.mcp_notes.server.mcp.NoteTool;
import br.edu.ifpr.mcp_notes.server.service.NoteService;

/* Lista tds as notas. */
public class ListNotesTool extends NoteTool {

   public ListNotesTool(NoteService service) {
      super(service);
   }

   @Override public String name()           { return "lista_notas"; }
   @Override public String description()    { return "Lista todas as notas. Sem args."; }
   @Override public String schemaResource() {
      return "/json-schemas/list-notes.schema.json";
   }

   @Override
   public String run(Map<String,Object> arguments) {
      return formatAll(service.list());
   }
}
