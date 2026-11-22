package br.edu.ifpr.mcp_notes.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.edu.ifpr.mcp_notes.server.domain.Note;

// Roda com um arquivo SQLite de verdade, numa pasta temporária.
class SqliteNoteRepositoryTest {
   @TempDir
   Path tempDir;

   // Um repository vazio (sem as notas de exemplo)
   private SqliteNoteRepository empty() {
      return new SqliteNoteRepository(tempDir.resolve("empty.db"),false);
   }

   // ---- as operações ------------------------------------------------------

   @Test
   @DisplayName("insert: devolve a nota com ids gerados, que vao crescendo")
   void insert() {
      SqliteNoteRepository repository = empty();
      Note                 first      = repository.insert("a","1");
      Note                 second     = repository.insert("b","2");

      assertEquals("a",first.title());
      assertEquals("1",first.content());
      assertTrue(second.id() > first.id());
   }

   @Test
   @DisplayName("findById: a nota guardada, ou vazio se o id nao existe")
   void findById() {
      SqliteNoteRepository repository = empty();
      Note                 stored     = repository.insert("a","1");

      assertEquals(Optional.of(stored),repository.findById(stored.id()));
      assertEquals(Optional.empty(),repository.findById(12345));
   }

   @Test
   @DisplayName("findAll: lista vazia, depois todas as notas ordenadas por id")
   void findAll() {
      SqliteNoteRepository repository = empty();

      assertTrue(repository.findAll().isEmpty());

      Note first  = repository.insert("a","1");
      Note second = repository.insert("b","2");

      assertEquals(List.of(first,second),repository.findAll());
   }

   @Test
   @DisplayName("update: true e salva se a nota existe, senao false")
   void update() {
      SqliteNoteRepository repository = empty();
      Note                 stored     = repository.insert("a","1");
      Note                 changed    = new Note(stored.id(),"b","2");

      assertTrue(repository.update(changed));
      assertEquals(Optional.of(changed),repository.findById(stored.id()));
      assertFalse(repository.update(new Note(999,"x","y")));
   }

   @Test
   @DisplayName("deleteById: true e some se a nota existe, senao false")
   void deleteById() {
      SqliteNoteRepository repository = empty();
      Note                 stored     = repository.insert("a","1");

      assertTrue(repository.deleteById(stored.id()));
      assertEquals(Optional.empty(),repository.findById(stored.id()));
      assertFalse(repository.deleteById(stored.id()));
   }

   @Test
   @DisplayName("texto com aspas, acento e SQL e guardado exatamente igual")
   void specialCharacters() {
      SqliteNoteRepository repository = empty();
      Note                 stored     = repository.insert("It's \"quoted\"",
                                                          "caf\u00e9; DROP TABLE");

      assertEquals(Optional.of(stored),repository.findById(stored.id()));
   }

   // ---- criação e notas de exemplo ----------------------------------------

   @Test
   @DisplayName("um banco novo recebe as notas de exemplo")
   void newDatabaseHasSamples() {
      SqliteNoteRepository repository =
                    new SqliteNoteRepository(tempDir.resolve("a.db"),true);

      assertEquals(3,repository.findAll().size());
      assertEquals("Bem vindo",repository.findAll().get(0).title());
   }

   @Test
   @DisplayName("abrir um banco que ja existe nao adiciona os exemplos de novo")
   void samplesAreNotDuplicated() {
      Path file = tempDir.resolve("b.db");

      new SqliteNoteRepository(file,true);

      assertEquals(3,new SqliteNoteRepository(file,true).findAll().size());
   }

   @Test
   @DisplayName("os exemplos que foram excluidos nao voltam")
   void deletedSamplesStayDeleted() {
      Path                 file       = tempDir.resolve("c.db");
      SqliteNoteRepository repository = new SqliteNoteRepository(file,true);

      repository.findAll().forEach(n -> repository.deleteById(n.id()));

      assertTrue(new SqliteNoteRepository(file,true).findAll().isEmpty());
   }

   @Test
   @DisplayName("a pasta de cima e criada se nao existe")
   void createsParentFolder() {
      Path file = tempDir.resolve("sub").resolve("folder").resolve("d.db");

      new SqliteNoteRepository(file,false);

      assertTrue(Files.isRegularFile(file));
   }
}
