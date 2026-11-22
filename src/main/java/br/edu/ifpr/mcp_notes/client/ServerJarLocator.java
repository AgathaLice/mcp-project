package br.edu.ifpr.mcp_notes.client;

import java.nio.file.Files;
import java.nio.file.Path;

/*
 * Acha o Jar do server pro client começar. A ordem é:
 *   a propriedade do sistema {@code mcp.server.jar};
 *   o arquivo {@code mcp-notes-server.jar} perto do Jar do client (o
 *       layout produzido pelo Maven build no target/dist);
 *   {@code target/dist/mcp-notes-server.jar} no diretorio funcionando
 *       (pra quando o client é iniciado pelo Eclipse).
 */
public final class ServerJarLocator {
   public static final String JAR_PROPERTY    = "mcp.server.jar";
   public static final String SERVER_JAR_NAME = "mcp-notes-server.jar";

   private ServerJarLocator() {}

   public static Path locate() {
      return resolve(System.getProperty(JAR_PROPERTY),
                     codeSourceLocation(),
                     Path.of("").toAbsolutePath());
   }

   // package-private pros testes unitários
   static Path resolve(String property, Path codeSource, Path workingDir) {
      if (property != null && !property.isBlank())
         return Path.of(property.trim());

      if (codeSource != null && Files.isRegularFile(codeSource) &&
          codeSource.toString().endsWith(".jar"))
         return codeSource.resolveSibling(SERVER_JAR_NAME);

      return workingDir.resolve("target").resolve("dist")
                       .resolve(SERVER_JAR_NAME);
   }

   private static Path codeSourceLocation() {
      try {
         return Path.of(ServerJarLocator.class.getProtectionDomain()
                                              .getCodeSource()
                                              .getLocation()
                                              .toURI());
      }
      catch (Exception e) {
         return null;
      }
   }
}
