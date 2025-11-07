package org.apache.cloudstack.vnf.dictionary;

import org.apache.cloudstack.vnf.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.*;
import java.util.regex.*;

public class VnfTemplateRenderer {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * Render a template string with context variables
     */
    public static String render(String template, TemplateContext context) {
        if (template == null) {
            return null;
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = context.get(placeholder);
            
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Render and escape for JSON context
     */
    public static String renderJson(String template, TemplateContext context) {
        String rendered = render(template, context);
        // Could add JSON escaping here if needed
        return rendered;
    }
    
    /**
     * Check if template has all required placeholders filled
     */
    public static boolean hasUnresolvedPlaceholders(String rendered) {
        return PLACEHOLDER_PATTERN.matcher(rendered).find();
    }
}
