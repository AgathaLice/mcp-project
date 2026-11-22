package br.edu.ifpr.mcp_notes.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import br.edu.ifpr.mcp_notes.server.domain.Note;
import br.edu.ifpr.mcp_notes.server.exception.AppException;
import br.edu.ifpr.mcp_notes.support.InMemoryNoteRepository;

class NoteServiceTest {
   private NoteService service;

   @BeforeEach
   void setUp() {
      service = new NoteService(new InMemoryNoteRepository());
   }

   // Roda o código, espera uma AppException e devolve a mensagem dela
   private String errorOf(Executable code) {
      return assertThrows(AppException.class,code).getMessage();
   }

   // ---- add ---------------------------------------------------------------

   @Test
   @DisplayName("add: guarda a nota (sem espacos nas pontas) e devolve com o id")
   void add() {
      assertEquals(new Note(1,"Titulo","Texto"),
                   service.add("  Titulo ","Texto  "));
   }

   @Test
   @DisplayName("add: titulo/texto vazio, em branco ou null sao rejeitados")
   void addRejectsEmpty() {
      assertEquals("O 'titulo' nao pode estar vazio.",
                   errorOf(() -> service.add(null,"c")));
      assertEquals("O 'titulo' nao pode estar vazio.",
                   errorOf(() -> service.add("  ","c")));
      assertEquals("O 'texto' nao pode estar vazio.",
                   errorOf(() -> service.add("t",null)));
      assertEquals("O 'texto' nao pode estar vazio.",
                   errorOf(() -> service.add("t","")));
   }

   @Test
   @DisplayName("add: o tamanho maximo e aceito, um a mais e rejeitado")
   void addLimits() {
      String title = "a".repeat(NoteService.MAX_TITLE_LENGTH);

      assertEquals(title,service.add(title,"c").title());
      assertEquals("O 'titulo' precisa ter no maximo 100 caracteres.",
                   errorOf(() -> service.add(title + "a","c")));
      assertEquals("O 'texto' precisa ter no maximo 2000 caracteres.",
                   errorOf(() -> service.add("t","a".repeat(2001))));
   }

   // ---- get / list --------------------------------------------------------

   @Test
   @DisplayName("get: devolve uma nota que existe, id desconhecido da erro")
   void get() {
      Note added = service.add("t","c");

      assertEquals(added,service.get(added.id()));
      assertEquals("Nota com o ID 99 nao foi encontrada.",
                   errorOf(() -> service.get(99)));
   }

   @Test
   @DisplayName("list: vazia, depois todas na ordem que foram inseridas")
   void list() {
      assertTrue(service.list().isEmpty());

      service.add("primeira","c");
      service.add("segunda","c");

      assertEquals(2,service.list().size());
      assertEquals("primeira",service.list().get(0).title());
   }

   // ---- update ------------------------------------------------------------

   @Test
   @DisplayName("update: muda titulo e texto, ou so um dos dois")
   void update() {
      Note added = service.add("velho","texto velho");

      assertEquals(new Note(1,"novo","texto novo"),
                   service.update(added.id(),"novo","texto novo"));
      assertEquals(new Note(1,"mais novo","texto novo"),
                   service.update(added.id(),"mais novo",null));
      assertEquals(new Note(1,"mais novo","novissimo"),
                   service.update(added.id(),null,"novissimo"));
      assertEquals(service.get(1),new Note(1,"mais novo","novissimo"));
   }

   @Test
   @DisplayName("update: nada pra mudar, valor em branco e id desconhecido")
   void updateErrors() {
      Note added = service.add("t","c");

      assertEquals("Adicione ao menos 'titulo' ou 'texto' para atualizar.",
                   errorOf(() -> service.update(added.id(),null,null)));
      assertEquals("O 'titulo' nao pode estar vazio.",
                   errorOf(() -> service.update(added.id()," ",null)));
      assertEquals("Nota com o ID 99 nao foi encontrada.",
                   errorOf(() -> service.update(99,"t","c")));
   }

   // ---- delete ------------------------------------------------------------

   @Test
   @DisplayName("delete: remove a nota, id desconhecido da erro")
   void delete() {
      Note added = service.add("t","c");

      service.delete(added.id());

      assertTrue(service.list().isEmpty());
      assertEquals("Nota com o ID 1 nao foi encontrada.",
                   errorOf(() -> service.delete(added.id())));
   }
}
