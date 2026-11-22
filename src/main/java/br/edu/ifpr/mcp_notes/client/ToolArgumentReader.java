package br.edu.ifpr.mcp_notes.client;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Pede pro user os args da tool, só olha pro JSON das tool. 
 * Não conhece as tools por nome, então pode rodas até futuras
 * adicionadas que não conhece ainda (facilita se eu for adicionar mais coisa)
 */
public class ToolArgumentReader {
   private final Scanner     scanner;
   private final PrintStream out;

   public ToolArgumentReader(Scanner scanner, PrintStream out) {
      this.scanner = scanner;
      this.out     = out;
   }

   public Map<String,Object> read(Map<String,Object> schema) {
      Map<String,Object> arguments = new LinkedHashMap<>(); 
      // pesquisar mais sobre hash, é um saco de usar

      if (schema == null ||
          !(schema.get("properties") instanceof Map<?,?> properties) ||
          properties.isEmpty())
         return arguments;

      Set<String> required = requiredNames(schema);

      out.println();
      out.println("Insira os args:");
      for (Map.Entry<?,?> entry : properties.entrySet()) {
         if (!(entry.getValue() instanceof Map<?,?> property))
            continue;

         String name  = String.valueOf(entry.getKey());
         Object value = ask(name,property,required.contains(name));

         if (value != null)
            arguments.put(name,value);
      }
      return arguments;
   }

   static Set<String> requiredNames(Map<String,Object> schema) {
      if (schema.get("required") instanceof List<?> list)
         return list.stream().map(String::valueOf).collect(Collectors.toSet());

      return Set.of();
   }

   static Object convert(String value, String type) {
      return switch (type) {
         case "integer" -> Long.valueOf(value);
         case "number"  -> Double.valueOf(value);
         case "boolean" -> parseBoolean(value);
         case "string"  -> value;
         default        -> throw new IllegalArgumentException(
                                       "Tipo sem suporte: " +
                                       type);
      };
   }

   private static Boolean parseBoolean(String value) {
      if ("true".equalsIgnoreCase(value) || "sim".equalsIgnoreCase(value) ||
          "yes".equalsIgnoreCase(value))
         return true;

      if ("false".equalsIgnoreCase(value) || "nao".equalsIgnoreCase(value) ||
          "no".equalsIgnoreCase(value))
         return false;

      throw new IllegalArgumentException("Insira true/false ou sim/nao");
   }

   private static String textOf(Map<?,?> map, String key, String fallback) {
      Object value = map.get(key);

      return value == null ? fallback : String.valueOf(value);
   }

   private Object ask(String name, Map<?,?> property, boolean required) {
      String type        = textOf(property,"type","string");
      String description = textOf(property,"description","");

      while (true) {
         out.println();
         out.println("'" + name + "' [" + type + "] " +
                     (required ? "(necessario)" : "(opcional)"));
         if (!description.isEmpty())
            out.println("Descricao: " + description);
         out.print("Valor: ");
         out.flush();

         if (!scanner.hasNextLine())
            return null;

         String text = scanner.nextLine().trim();

         if (text.isEmpty()) {
            if (!required)
               return null;

            out.println("Isso e necessario");
            continue;
         }
         try {
            return convert(text,type);
         }
         catch (IllegalArgumentException e) {
            out.println("Valor invalido: " + e.getMessage());
         }
      }
   }
}
