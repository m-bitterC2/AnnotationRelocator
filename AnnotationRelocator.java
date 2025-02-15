import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationRelocator {
    static class FieldInfo {
        String fieldName;
        String fieldType;
        String adapter;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java UpdateAnnotations <directory>");
            return;
        }

        Path dir = Paths.get(args[0]);
        if (!Files.isDirectory(dir)) {
            System.out.println("Provided path is not a directory.");
            return;
        }

        try {
            Files.walk(dir)
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .forEach(AnnotationRelocator::processFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(Path file) {
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            String updatedContent = updateContent(content);
            if (!content.equals(updatedContent)) {
                Files.write(file, updatedContent.getBytes(StandardCharsets.UTF_8));
                System.out.println("Updated file: " + file);
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + file);
            e.printStackTrace();
        }
    }

    private static String updateContent(String content) {
        List<FieldInfo> fields = new ArrayList<>();
        Pattern blockPattern = Pattern.compile(
                "(?m)^((?:\\s*@.*\\r?\\n)+)(\\s*private\\s+(Date|Time|Timestamp)\\s+(\\w+)\\s*;)");
        Matcher blockMatcher = blockPattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (blockMatcher.find()) {
            String annotationBlock = blockMatcher.group(1);
            String fieldDeclaration = blockMatcher.group(2);
            String fieldType = blockMatcher.group(3);
            String fieldName = blockMatcher.group(4);

            Pattern adapterLinePattern = Pattern
                    .compile("(?m)^\\s*@JsonbTypeAdapter\\s*\\(\\s*([^)]*)\\s*\\)\\s*$\\r?\\n?");
            Matcher adapterLineMatcher = adapterLinePattern.matcher(annotationBlock);
            String adapterValue = null;
            if (adapterLineMatcher.find()) {
                adapterValue = adapterLineMatcher.group(1);
            }
            annotationBlock = adapterLinePattern.matcher(annotationBlock).replaceAll("");

            if (adapterValue != null) {
                FieldInfo info = new FieldInfo();
                info.fieldName = fieldName;
                info.fieldType = fieldType;
                info.adapter = adapterValue;
                fields.add(info);
            }
            blockMatcher.appendReplacement(sb,
                    Matcher.quoteReplacement(annotationBlock + fieldDeclaration));
        }
        blockMatcher.appendTail(sb);
        content = sb.toString();

        for (FieldInfo info : fields) {
            String capName =
                    Character.toUpperCase(info.fieldName.charAt(0)) + info.fieldName.substring(1);
            String annotationLine = "@JsonbTypeAdapter(" + info.adapter + ")";

            String getterRegex = "(?m)^(\\s*)(public\\s+" + Pattern.quote(info.fieldType)
                    + "\\s+get" + Pattern.quote(capName) + "\\s*\\(\\s*\\)\\s*\\{)";
            Pattern getterPattern = Pattern.compile(getterRegex);
            Matcher getterMatcher = getterPattern.matcher(content);
            sb = new StringBuffer();
            while (getterMatcher.find()) {
                String indent = getterMatcher.group(1);
                String replacement = indent + annotationLine + indent + getterMatcher.group(2);
                getterMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            getterMatcher.appendTail(sb);
            content = sb.toString();

            String setterRegex = "(?m)^(\\s*)(public\\s+void\\s+set" + Pattern.quote(capName)
                    + "\\s*\\(\\s*" + Pattern.quote(info.fieldType) + "\\s+\\w+\\s*\\)\\s*\\{)";
            Pattern setterPattern = Pattern.compile(setterRegex);
            Matcher setterMatcher = setterPattern.matcher(content);
            sb = new StringBuffer();
            while (setterMatcher.find()) {
                String indent = setterMatcher.group(1);
                String replacement = indent + annotationLine + indent + setterMatcher.group(2);
                setterMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            setterMatcher.appendTail(sb);
            content = sb.toString();
        }

        return content;
    }
}
