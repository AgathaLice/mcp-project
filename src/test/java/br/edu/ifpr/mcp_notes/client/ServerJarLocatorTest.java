package br.edu.ifpr.mcp_notes.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerJarLocatorTest {
   @TempDir
   Path tempDir;

   @Test
   @DisplayName("a propriedade do sistema tem prioridade")
   void propertyWins() {
      assertEquals(Path.of("custom.jar"),
                   ServerJarLocator.resolve("custom.jar",null,tempDir));
   }

   @Test
   @DisplayName("rodando de um jar: o jar do server fica do lado dele")
   void siblingOfClientJar() throws IOException {
      Path clientJar = Files.createFile(tempDir.resolve("mcp-notes-client.jar"));

      assertEquals(tempDir.resolve(ServerJarLocator.SERVER_JAR_NAME),
                   ServerJarLocator.resolve(null,clientJar,tempDir));
   }

   @Test
   @DisplayName("rodando de uma pasta classes: target/dist no diretorio atual")
   void classesFolderFallback() {
      Path classes = tempDir.resolve("classes");

      assertEquals(tempDir.resolve("target").resolve("dist")
                          .resolve(ServerJarLocator.SERVER_JAR_NAME),
                   ServerJarLocator.resolve(" ",classes,tempDir));
   }

   @Test
   @DisplayName("origem do codigo desconhecida: target/dist no diretorio atual")
   void unknownCodeSource() {
      assertEquals(tempDir.resolve("target").resolve("dist")
                          .resolve(ServerJarLocator.SERVER_JAR_NAME),
                   ServerJarLocator.resolve(null,null,tempDir));
   }
}
