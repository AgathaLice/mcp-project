package br.edu.ifpr.mcp_notes.server.mcp.tools;

import java.util.Map;

import br.edu.ifpr.mcp_notes.server.mcp.NoteTool;
import br.edu.ifpr.mcp_notes.server.service.NoteService;

/* Deleta uma nota. */
public class DeleteNoteTool extends NoteTool {

   public DeleteNoteTool(NoteService service) {
      super(service);
   }

   @Override public String name()           { return "exclui_nota"; }
   @Override public String description()    { return "Permanentemente exclui uma nota com o dado ID."; }
   @Override public String schemaResource() {
      return "/json-schemas/delete-note.schema.json";
   }

   @Override
   public String run(Map<String,Object> arguments) {
      long id = requireLong(arguments,"id");

      service.delete(id);

      return "Nota #" + id + " excluida.";
   }
}
