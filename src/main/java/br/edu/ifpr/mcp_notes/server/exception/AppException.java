package br.edu.ifpr.mcp_notes.server.exception;

/*
 * Criação de uma exceção para mostrar pro user no client
 * input invalido, nota não encontrada, problema de banco de dados
 * Outras exceções sãpo só bugs
 */
public class AppException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public AppException(String message) {
      super(message);
   }

   public AppException(String message, Throwable cause) {
      super(message,cause);
   }
}
