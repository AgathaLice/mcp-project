package br.edu.ifpr.mcp_notes.server.repository;

import java.util.List;
import java.util.Optional;

import br.edu.ifpr.mcp_notes.server.domain.Note;

/*
 * Abstração do armazenamento. O serviço depende dessa interface, não do SQLite
 * pros testes conseguirem usar a fake da memoria.
 */
public interface NoteRepository {
   // Guarda uma nota e retorna seu ID
   Note insert(String title, String content);

   Optional<Note> findById(long id);

   // Todas as notas ordenadas pelo ID
   List<Note> findAll();

   // @return true se uma nota com esse id existe e foi atualizada
   boolean update(Note note);

   // @return true se uma nota com esse id foi deletada
   boolean deleteById(long id);
}
