package com.joonsung.kson;

import com.intellij.openapi.vfs.VirtualFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ParseJson {

    public static void generateJavaClass(VirtualFile jsonFile, String savePath, String className) throws IOException {
        String jsonContent = new String(jsonFile.contentsToByteArray());
        JSONObject jsonObject = new JSONObject(jsonContent);

        StringBuilder classBuilder = new StringBuilder();
        Set<String> imports = new HashSet<>();
        Set<String> nestedClasses = new HashSet<>();

        classBuilder.append("public class ").append(className).append(" {\n");

        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = jsonObject.get(key);
            String javaType = getJavaType(value, key, nestedClasses);

            if (javaType.startsWith("List<")) {
                imports.add("import java.util.List;");
            }

            classBuilder.append("    private ").append(javaType).append(" ").append(key).append(";\n");
        }
        // Getter & Setter
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            classBuilder.append("\n");
            String key = it.next();
            Object value = jsonObject.get(key);
            String javaType = getJavaType(value, key, nestedClasses);
            String capitalizedKey = Character.toUpperCase(key.charAt(0)) + key.substring(1);

            // Getter
            classBuilder.append("    public ").append(javaType).append(" get").append(capitalizedKey).append("() {\n");
            classBuilder.append("        return ").append(key).append(";\n");
            classBuilder.append("    }\n");

            // Setter
            classBuilder.append("    public void set").append(capitalizedKey).append("(").append(javaType).append(" ").append(key).append(") {\n");
            classBuilder.append("        this.").append(key).append(" = ").append(key).append(";\n");
            classBuilder.append("    }");
        }

        classBuilder.append("}\n");

        try (FileWriter fileWriter = new FileWriter(savePath)) {
            // import
            for (String importStmt : imports) {
                fileWriter.write(importStmt + "\n");
            }
            fileWriter.write("\n");
            fileWriter.write(classBuilder.toString());
        }

        for (String nestedClass : nestedClasses) {
            if (jsonObject.get(nestedClass) instanceof JSONArray) {
                JSONArray jsonArray = jsonObject.getJSONArray(nestedClass);
                if (jsonArray.length() > 0 && jsonArray.get(0) instanceof JSONObject) {
                    String nestedClassName = Character.toUpperCase(nestedClass.charAt(0)) + nestedClass.substring(1);
                    JSONObject nestedJsonObject = jsonArray.getJSONObject(0);
                    generateNestedClass(nestedClassName, nestedJsonObject, savePath);
                }
            } else if (jsonObject.get(nestedClass) instanceof JSONObject) {
                String nestedClassName = Character.toUpperCase(nestedClass.charAt(0)) + nestedClass.substring(1);
                JSONObject nestedJsonObject = jsonObject.getJSONObject(nestedClass);
                generateNestedClass(nestedClassName, nestedJsonObject, savePath);
            }
        }
    }

    private static void generateNestedClass(String className, JSONObject jsonObject, String savePath) throws IOException {
        StringBuilder classBuilder = new StringBuilder();
        Set<String> imports = new HashSet<>();
        Set<String> nestedClasses = new HashSet<>();

        classBuilder.append("public class ").append(className).append(" {\n");

        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = jsonObject.get(key);
            String javaType = getJavaType(value, key, nestedClasses);

            if (javaType.startsWith("List<")) {
                imports.add("import java.util.List;");
            }

            classBuilder.append("    private ").append(javaType).append(" ").append(key).append(";\n");
        }

        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {

            String key = it.next();
            Object value = jsonObject.get(key);
            String javaType = getJavaType(value, key, nestedClasses);
            String capitalizedKey = Character.toUpperCase(key.charAt(0)) + key.substring(1);

            // Getter 메서드
            classBuilder.append("\n");
            classBuilder.append("    public ").append(javaType).append(" get").append(capitalizedKey).append("() {\n");
            classBuilder.append("        return ").append(key).append(";\n");
            classBuilder.append("    }\n");

            // Setter 메서드
            classBuilder.append("    public void set").append(capitalizedKey).append("(").append(javaType).append(" ").append(key).append(") {\n");
            classBuilder.append("        this.").append(key).append(" = ").append(key).append(";\n");
            classBuilder.append("    }");
        }

        classBuilder.append("}\n");

        String nestedSavePath = savePath.substring(0, savePath.lastIndexOf('/')) + "/" + className + ".java";

        try (FileWriter fileWriter = new FileWriter(nestedSavePath)) {
            for (String importStmt : imports) {
                fileWriter.write(importStmt + "\n");
            }
            fileWriter.write("\n");
            fileWriter.write(classBuilder.toString());
        }

        for (String nestedClass : nestedClasses) {
            if (jsonObject.get(nestedClass) instanceof JSONArray) {
                JSONArray jsonArray = jsonObject.getJSONArray(nestedClass);
                if (jsonArray.length() > 0 && jsonArray.get(0) instanceof JSONObject) {
                    String nestedClassName = Character.toUpperCase(nestedClass.charAt(0)) + nestedClass.substring(1);
                    JSONObject nestedJsonObject = jsonArray.getJSONObject(0);
                    generateNestedClass(nestedClassName, nestedJsonObject, nestedSavePath);
                }
            } else if (jsonObject.get(nestedClass) instanceof JSONObject) {
                String nestedClassName = Character.toUpperCase(nestedClass.charAt(0)) + nestedClass.substring(1);
                JSONObject nestedJsonObject = jsonObject.getJSONObject(nestedClass);
                generateNestedClass(nestedClassName, nestedJsonObject, nestedSavePath);
            }
        }
    }

    private static String getJavaType(Object value, String key, Set<String> nestedClasses) {
        if (value instanceof Integer) {
            return "int";
        } else if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Double || value instanceof Float) {
            return "double";
        } else if (value instanceof JSONArray) {
            nestedClasses.add(key);
            return "List<" + determineListType((JSONArray) value, key) + ">";
        } else if (value instanceof JSONObject) {
            nestedClasses.add(key);
            String nestedClassName = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            return nestedClassName ;
        } else {
            return "String";
        }
    }

    private static String determineListType(JSONArray jsonArray, String key) {
        if (jsonArray.length() == 0) {
            return "Object";
        }

        Object firstElement = jsonArray.get(0);

        if (firstElement instanceof Integer) {
            return "Integer";
        } else if (firstElement instanceof Boolean) {
            return "Boolean";
        } else if (firstElement instanceof Double || firstElement instanceof Float) {
            return "Double";
        } else if (firstElement instanceof JSONObject) {
            String nestedClassName = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            return nestedClassName ;
        } else {
            return "String";
        }
    }
}