package br.edu.ifpr.mcp_notes.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ToolArgumentReaderTest {
   // Tudo que o reader imprime fica aqui, pra gente conferir o texto
   private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

   // O Scanner le o "input" como se o user tivesse digitado
   private ToolArgumentReader readerWithInput(String input) {
      return new ToolArgumentReader(new Scanner(input),
                                    new PrintStream(captured));
   }

   // Monta um JSON schema na mão: nome1,tipo1,nome2,tipo2...
   private Map<String,Object> schema(List<String> required, String... namesAndTypes) {
      Map<String,Object> properties = new LinkedHashMap<>();

      for (int i = 0; i < namesAndTypes.length; i += 2)
         properties.put(namesAndTypes[i],Map.of("type",namesAndTypes[i + 1],
                                                "description","d"));

      Map<String,Object> schema = new LinkedHashMap<>();

      schema.put("type","object");
      schema.put("properties",properties);
      schema.put("required",required);

      return schema;
   }

   @Test
   @DisplayName("le uma string e um inteiro, convertendo os tipos")
   void readsValues() {
      Map<String,Object> result = readerWithInput("Oi\n42\n").read(
                schema(List.of("titulo","id"),"titulo","string","id","integer"));

      assertEquals("Oi",result.get("titulo"));
      assertEquals(42L,result.get("id"));
   }

   @Test
   @DisplayName("um arg opcional deixado em branco nao e enviado")
   void optionalBlank() {
      Map<String,Object> result = readerWithInput("\n").read(
                                  schema(List.of(),"nota","string"));

      assertTrue(result.isEmpty());
   }

   @Test
   @DisplayName("um arg necessario e perguntado de novo ate ser preenchido")
   void requiredAskedAgain() {
      Map<String,Object> result = readerWithInput("\n\nfinalmente\n").read(
                                  schema(List.of("a"),"a","string"));

      assertEquals("finalmente",result.get("a"));
      assertTrue(captured.toString().contains("Isso e necessario"));
   }

   @Test
   @DisplayName("um numero invalido e perguntado de novo")
   void invalidNumber() {
      Map<String,Object> result = readerWithInput("abc\n7\n").read(
                                  schema(List.of("n"),"n","integer"));

      assertEquals(7L,result.get("n"));
      assertTrue(captured.toString().contains("Valor invalido"));
   }

   @Test
   @DisplayName("sem properties ou sem schema: nao pergunta nada")
   void nothingToAsk() {
      assertTrue(readerWithInput("").read(Map.of("type","object")).isEmpty());
      assertTrue(readerWithInput("").read(null).isEmpty());
      assertTrue(readerWithInput("").read(
                         Map.of("properties",Map.of())).isEmpty());
   }

   @Test
   @DisplayName("fim do input deixa o arg de fora em vez de ficar em loop")
   void endOfInput() {
      assertTrue(readerWithInput("").read(
                         schema(List.of("a"),"a","string")).isEmpty());
   }

   @Test
   @DisplayName("requiredNames: com e sem a chave 'required'")
   void requiredNames() {
      assertEquals(Set.of("a","b"),ToolArgumentReader.requiredNames(
                                      Map.of("required",List.of("a","b"))));
      assertEquals(Set.of(),ToolArgumentReader.requiredNames(Map.of()));
   }

   @Test
   @DisplayName("convert: todos os tipos que tem suporte")
   void convertTypes() {
      assertEquals(5L,ToolArgumentReader.convert("5","integer"));
      assertEquals(2.5,ToolArgumentReader.convert("2.5","number"));
      assertEquals(true,ToolArgumentReader.convert("TRUE","boolean"));
      assertEquals(true,ToolArgumentReader.convert("sim","boolean"));
      assertEquals(true,ToolArgumentReader.convert("yes","boolean"));
      assertEquals(false,ToolArgumentReader.convert("false","boolean"));
      assertEquals(false,ToolArgumentReader.convert("nao","boolean"));
      assertEquals(false,ToolArgumentReader.convert("no","boolean"));
      assertEquals("x",ToolArgumentReader.convert("x","string"));
   }

   @Test
   @DisplayName("convert: valores invalidos e tipos sem suporte dao erro")
   void convertErrors() {
      assertThrows(IllegalArgumentException.class,
                   () -> ToolArgumentReader.convert("x","integer"));
      assertThrows(IllegalArgumentException.class,
                   () -> ToolArgumentReader.convert("x","number"));
      assertThrows(IllegalArgumentException.class,
                   () -> ToolArgumentReader.convert("talvez","boolean"));
      assertThrows(IllegalArgumentException.class,
                   () -> ToolArgumentReader.convert("x","array"));
   }
}
