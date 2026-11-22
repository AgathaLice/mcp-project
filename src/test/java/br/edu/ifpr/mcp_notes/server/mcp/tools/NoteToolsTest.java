package br.edu.ifpr.mcp_notes.server.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import br.edu.ifpr.mcp_notes.server.exception.AppException;
import br.edu.ifpr.mcp_notes.server.service.NoteService;
import br.edu.ifpr.mcp_notes.support.InMemoryNoteRepository;

/*
 * Testes unitários das cinco tools (sem MCP SDK): o texto que cada uma
 * devolve e todos os erros que ela pode dar.
 */
class NoteToolsTest {
   private NoteService      service;
   private AddNoteTool      add;
   private GetNoteTool      get;
   private ListNotesTool    list;
   private UpdateNoteTool   update;
   private DeleteNoteTool   delete;

   @BeforeEach
   void setUp() {
      service = new NoteService(new InMemoryNoteRepository());
      add     = new AddNoteTool(service);
      get     = new GetNoteTool(service);
      list    = new ListNotesTool(service);
      update  = new UpdateNoteTool(service);
      delete  = new DeleteNoteTool(service);
   }

   private String errorOf(Executable code) {
      return assertThrows(AppException.class,code).getMessage();
   }

   // ---- adiciona_nota -----------------------------------------------------

   @Test
   @DisplayName("adiciona_nota: cria a nota e mostra ela")
   void addSuccess() {
      assertEquals("Nota criada:\n[#1] T\nC",
                   add.run(Map.of("titulo","T","texto","C")));
      assertEquals(1,service.list().size());
   }

   @Test
   @DisplayName("adiciona_nota: arg faltando, tipo errado e em branco dao erro")
   void addErrors() {
      assertEquals("O arg 'titulo' é necessário.",
                   errorOf(() -> add.run(Map.of("texto","C"))));
      assertEquals("O arg 'texto' é necessário.",
                   errorOf(() -> add.run(Map.of("titulo","T"))));
      assertEquals("O arg 'titulo' precisa ser uma string.",
                   errorOf(() -> add.run(Map.of("titulo",1,"texto","C"))));
      assertEquals("O 'titulo' nao pode estar vazio.",
                   errorOf(() -> add.run(Map.of("titulo"," ","texto","C"))));
      assertEquals("O arg 'titulo' é necessário.",
                   errorOf(() -> add.run(null)));
   }

   // ---- pega_nota ---------------------------------------------------------

   @Test
   @DisplayName("pega_nota: devolve a nota; aceita id decimal inteiro")
   void getSuccess() {
      service.add("T","C");

      assertEquals("[#1] T\nC",get.run(Map.of("id",1)));
      assertEquals("[#1] T\nC",get.run(Map.of("id",1L)));
      assertEquals("[#1] T\nC",get.run(Map.of("id",1.0)));
   }

   @Test
   @DisplayName("pega_nota: id desconhecido, faltando ou invalido dao erro")
   void getErrors() {
      assertEquals("Nota com o ID 42 nao foi encontrada.",
                   errorOf(() -> get.run(Map.of("id",42))));
      assertEquals("O arg 'id' é necessário.",
                   errorOf(() -> get.run(Map.of())));
      assertEquals("O arg 'id' precisa ser um inteiro.",
                   errorOf(() -> get.run(Map.of("id","abc"))));
      assertEquals("O arg 'id' precisa ser um inteiro.",
                   errorOf(() -> get.run(Map.of("id",1.5))));
      assertEquals("O arg 'id' precisa ser um inteiro.",
                   errorOf(() -> get.run(Map.of("id",Double.NaN))));
      assertEquals("O arg 'id' precisa ser um inteiro.",
                   errorOf(() -> get.run(Map.of("id",Double.POSITIVE_INFINITY))));
   }

   // ---- lista_notas -------------------------------------------------------

   @Test
   @DisplayName("lista_notas: 'Sem notas encontradas' quando vazio, senao todas")
   void listNotes() {
      assertEquals("Sem notas encontradas",list.run(Map.of()));
      assertEquals("Sem notas encontradas",list.run(null));

      service.add("a","x");
      service.add("b","y");

      assertEquals("[#1] a\nx\n\n[#2] b\ny",list.run(Map.of()));
   }

   // ---- atualiza_nota -----------------------------------------------------

   @Test
   @DisplayName("atualiza_nota: muda os dois campos ou so um")
   void updateSuccess() {
      service.add("titulo velho","texto velho");

      assertEquals("Nota atualizada:\n[#1] novo\ntexto novo",
                   update.run(Map.of("id",1,"titulo","novo","texto","texto novo")));
      assertEquals("Nota atualizada:\n[#1] outro\ntexto novo",
                   update.run(Map.of("id",1,"titulo","outro")));
      assertEquals("Nota atualizada:\n[#1] outro\nfinal",
                   update.run(Map.of("id",1,"texto","final")));
   }

   @Test
   @DisplayName("atualiza_nota: todos os casos de erro")
   void updateErrors() {
      service.add("t","c");

      assertEquals("Adicione ao menos 'titulo' ou 'texto' para atualizar.",
                   errorOf(() -> update.run(Map.of("id",1))));
      assertEquals("Nota com o ID 9 nao foi encontrada.",
                   errorOf(() -> update.run(Map.of("id",9,"titulo","x"))));
      assertEquals("O arg 'id' é necessário.",
                   errorOf(() -> update.run(Map.of("titulo","x"))));
      assertEquals("O 'titulo' nao pode estar vazio.",
                   errorOf(() -> update.run(Map.of("id",1,"titulo"," "))));
      assertEquals("O arg 'texto' precisa ser uma string.",
                   errorOf(() -> update.run(Map.of("id",1,"texto",5))));
   }

   // ---- exclui_nota -------------------------------------------------------

   @Test
   @DisplayName("exclui_nota: exclui a nota")
   void deleteSuccess() {
      service.add("t","c");

      assertEquals("Nota #1 excluida.",delete.run(Map.of("id",1)));
      assertEquals(0,service.list().size());
   }

   @Test
   @DisplayName("exclui_nota: id desconhecido, faltando ou invalido dao erro")
   void deleteErrors() {
      assertEquals("Nota com o ID 5 nao foi encontrada.",
                   errorOf(() -> delete.run(Map.of("id",5))));
      assertEquals("O arg 'id' é necessário.",
                   errorOf(() -> delete.run(Map.of())));
      assertEquals("O arg 'id' precisa ser um inteiro.",
                   errorOf(() -> delete.run(Map.of("id","um"))));
   }
}
