package br.edu.ifpr.mcp_notes.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.ifpr.mcp_notes.server.domain.Note;
import br.edu.ifpr.mcp_notes.server.repository.NoteRepository;

/*
 * Fake escrita na mão que os testes unitários usam no lugar de uma lib de mock:
 * um repository de verdade, que funciona, mas guarda as notas só na memória.
 */
public class InMemoryNoteRepository implements NoteRepository {
   private final Map<Long,Note> notes  = new LinkedHashMap<>();
   private long                 nextId = 1;

   @Override
   public Note insert(String title, String content) {
      Note note = new Note(nextId++,title,content);

      notes.put(note.id(),note);

      return note;
   }

   @Override
   public Optional<Note> findById(long id) {
      return Optional.ofNullable(notes.get(id));
   }

   @Override
   public List<Note> findAll() {
      return new ArrayList<>(notes.values());
   }

   @Override
   public boolean update(Note note) {
      if (!notes.containsKey(note.id()))
         return false;

      notes.put(note.id(),note);

      return true;
   }

   @Override
   public boolean deleteById(long id) {
      return notes.remove(id) != null;
   }
}
