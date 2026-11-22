package br.edu.ifpr.mcp_notes.server.service;

import java.util.List;

import br.edu.ifpr.mcp_notes.server.domain.Note;
import br.edu.ifpr.mcp_notes.server.exception.AppException;
import br.edu.ifpr.mcp_notes.server.repository.NoteRepository;

/*
 * regras de nogócios das notas. Só depende do {@link NoteRepository}
 * interface: sabe nada do MCP e SQL.
 */
public class NoteService {
   public static final int MAX_TITLE_LENGTH   = 100;
   public static final int MAX_CONTENT_LENGTH = 2000;

   private final NoteRepository repository;

   public NoteService(NoteRepository repository) {
      this.repository = repository;
   }

   public Note add(String title, String content) {
      return repository.insert(clean(title,"titulo",MAX_TITLE_LENGTH),
                               clean(content,"texto",MAX_CONTENT_LENGTH));
   }

   public Note get(long id) {
      return repository.findById(id).orElseThrow(() -> notFound(id));
   }

   public List<Note> list() {
      return repository.findAll();
   }

   // Um titulo ou texto null significa "manter o valor atual".
   public Note update(long id, String title, String content) {
      if (title == null && content == null)
         throw new AppException("Adicione ao menos 'titulo' ou " +
                                "'texto' para atualizar.");

      String newTitle   = title == null ? null
                                        : clean(title,"titulo",MAX_TITLE_LENGTH);
      String newContent = content == null ? null
                                          : clean(content,"texto",
                                                  MAX_CONTENT_LENGTH);
      Note   current    = get(id);
      Note   updated    = new Note(id,
                                   newTitle == null ? current.title() : newTitle,
                                   newContent == null ? current.content()
                                                      : newContent);

      if (!repository.update(updated))
         throw notFound(id);

      return updated;
   }

   public void delete(long id) {
      if (!repository.deleteById(id))
         throw notFound(id);
   }

   private String clean(String value, String field, int maxLength) {
      if (value == null || value.isBlank())
         throw new AppException("O '" + field + "' nao pode estar vazio.");

      String text = value.strip();

      if (text.length() > maxLength)
         throw new AppException("O '" + field + "' precisa ter no maximo " +
                                maxLength + " caracteres.");

      return text;
   }

   private AppException notFound(long id) {
      return new AppException("Nota com o ID " + id + " nao foi encontrada.");
   }
}
