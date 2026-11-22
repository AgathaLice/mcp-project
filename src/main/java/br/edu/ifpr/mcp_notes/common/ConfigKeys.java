package br.edu.ifpr.mcp_notes.common;

/**
 * Nomes das entradas das configs divididas pelo client e serber.
 * Server le a localização do banco da propriedade do sistema ou da
 * variavel de enviroment. o client usa os mesmos nomes pra
 * passar o valor pro processo do server que ele inicia.
 */
public final class ConfigKeys {
   public static final String DB_PATH_PROPERTY = "mcp.db.path";
   public static final String DB_PATH_ENV_VAR  = "MCP_DB_PATH";

   private ConfigKeys() {}
}
