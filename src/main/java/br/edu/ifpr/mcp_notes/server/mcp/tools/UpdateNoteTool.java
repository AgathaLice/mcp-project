package br.edu.ifpr.mcp_notes.server.mcp.tools;

import java.util.Map;

import br.edu.ifpr.mcp_notes.server.mcp.NoteTool;
import br.edu.ifpr.mcp_notes.server.service.NoteService;

// Muda o titulo e/ou texto de uma nota, dado seu ID
public class UpdateNoteTool extends NoteTool {

   public UpdateNoteTool(NoteService service) {
      super(service);
   }

   @Override public String name()           { return "atualiza_nota"; }
   @Override public String schemaResource() {
      return "/json-schemas/update-note.schema.json";
   }
   @Override public String description() {
      return "Muda o titulo e/ou texto de uma nota, dado seu ID. " +
             "Args nao dados mantem seu conteudo";
   }

   @Override
   public String run(Map<String,Object> arguments) {
      return "Nota atualizada:\n" + format(service.update(
                                 requireLong(arguments,"id"),
                                 optionalString(arguments,"titulo"),
                                 optionalString(arguments,"texto")));
   }
}
