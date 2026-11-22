package br.edu.ifpr.mcp_notes.server.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.edu.ifpr.mcp_notes.server.domain.Note;
import br.edu.ifpr.mcp_notes.server.exception.AppException;

/**
 * SQLite usando JDBC. O contrutor cria a tabela e, só quando o
 * arquivo do banco de dados não existe ainda, as notas basees.
 * Tá em inglês pq é mais fácil de pesquisar sobre (e causei uns
 * erros quando tentei traduzir depois de fazer tudo antes das viagens)
 */
public final class SqliteNoteRepository implements NoteRepository {
   private static final String CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS notes (" +
      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
      "title TEXT NOT NULL, content TEXT NOT NULL)";

   private static final String[][] SAMPLES = {
      { "Bem vindo","Isso é uma nota de exemplo criada com o banco de dados." },
      { "Lista de compras","Leite, pão, manteiga, queijo, tomate, feijão." },
      { "Plano de estudos","Que confuso que é fazer um servidor MCP, pesquisar mais." }
   };

   // Uma operação JDBC num statement preparado
   private interface SqlTask<T> {
      T run(PreparedStatement statement) throws SQLException;
   }

   private final String url;

   public SqliteNoteRepository(Path file, boolean addSamples) {
      Path    absolute = file.toAbsolutePath();
      boolean isNew    = Files.notExists(absolute);

      url = "jdbc:sqlite:" + absolute;
      try {
         Files.createDirectories(absolute.getParent());
      }
      catch (IOException e) {
         throw new AppException("Não conseguiu criar o folder do banco de dados.",e);
      }
      execute(CREATE_TABLE,statement -> statement.executeUpdate());

      if (isNew && addSamples)
         for (String[] sample : SAMPLES)
            insert(sample[0],sample[1]);
   }

   @Override
   public Note insert(String title, String content) {
      return execute("INSERT INTO notes (title, content) VALUES (?, ?)",ps -> {
         ps.setString(1,title);
         ps.setString(2,content);
         ps.executeUpdate();

         // mesma conexão, então retorna o DI da linha que acabou de ser inserida
         try (Statement s  = ps.getConnection().createStatement();
              ResultSet rs = s.executeQuery("SELECT last_insert_rowid()")) {
            rs.next();

            return new Note(rs.getLong(1),title,content);
         }
      });
   }

   @Override
   public Optional<Note> findById(long id) {
      return execute("SELECT id, title, content FROM notes WHERE id = ?",ps -> {
         ps.setLong(1,id);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(toNote(rs)) : Optional.empty();
         }
      });
   }

   @Override
   public List<Note> findAll() {
      return execute("SELECT id, title, content FROM notes ORDER BY id",ps -> {
         List<Note> notes = new ArrayList<>();

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next())
               notes.add(toNote(rs));
         }
         return notes;
      });
   }

   @Override
   public boolean update(Note note) {
      return execute("UPDATE notes SET title = ?, content = ? WHERE id = ?",ps -> {
         ps.setString(1,note.title());
         ps.setString(2,note.content());
         ps.setLong(3,note.id());

         return ps.executeUpdate() > 0;
      });
   }

   @Override
   public boolean deleteById(long id) {
      return execute("DELETE FROM notes WHERE id = ?",ps -> {
         ps.setLong(1,id);

         return ps.executeUpdate() > 0;
      });
   }

   // Abre uma conexão, roda a task e fecha tudo, sempre
   private <T> T execute(String sql, SqlTask<T> task) {
      try (Connection        connection = DriverManager.getConnection(url);
           PreparedStatement statement  = connection.prepareStatement(sql)) {
         return task.run(statement);
      }
      catch (SQLException e) {
         throw new AppException("O banco de dados falhou.",e);
      }
   }

   private Note toNote(ResultSet rs) throws SQLException {
      return new Note(rs.getLong("id"),rs.getString("title"),
                      rs.getString("content"));
   }
}
