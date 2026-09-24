import java.util.*;
import java.util.regex.*;

public class TACOptimizer {

    public List<String> optimize(List<String> rawInstructions) {
        List<String> current = new ArrayList<>(rawInstructions);
        boolean changed = true;
        int maxPasses = 10; // Prevent infinite loops

        while (changed && maxPasses-- > 0) {
            int beforeSize = current.size();
            current = constantFoldAndPropagate(current);
            current = eliminateDeadTemporaries(current);
            changed = current.size() != beforeSize;
        }

        return current;
    }

    private List<String> constantFoldAndPropagate(List<String> instructions) {
        List<String> optimized = new ArrayList<>();
        Map<String, String> constTable = new HashMap<>();

        // Matches: t1 = 2 + 3, t2 = 5 * 4, etc.
        Pattern foldPattern = Pattern.compile("^([a-zA-Z0-9_]+|t\\d+)\\s*=\\s*(-?\\d+)\\s*([+\\-*/%])\\s*(-?\\d+)$");
        // Matches: var = literal or var = var
        Pattern assignPattern = Pattern.compile("^([a-zA-Z0-9_]+|t\\d+)\\s*=\\s*(.+)$");

        for (String line : instructions) {
            String trimmed = line.trim();

            // Do not propagate across labels or jump conditions to maintain safety
            if (trimmed.endsWith(":") || trimmed.startsWith("IF_FALSE") || trimmed.startsWith("GOTO") || trimmed.startsWith("CALL")) {
                constTable.clear();
                optimized.add(line);
                continue;
            }

            // Substitute known constants into right-hand side
            for (Map.Entry<String, String> entry : constTable.entrySet()) {
                trimmed = replaceWord(trimmed, entry.getKey(), entry.getValue());
            }

            // Attempt Constant Folding: e.g. t1 = 2 + 3 -> t1 = 5
            Matcher foldMatcher = foldPattern.matcher(trimmed);
            if (foldMatcher.matches()) {
                String target = foldMatcher.group(1);
                long left = Long.parseLong(foldMatcher.group(2));
                String op = foldMatcher.group(3);
                long right = Long.parseLong(foldMatcher.group(4));

                Long evaluated = evaluate(left, op, right);
                if (evaluated != null) {
                    trimmed = target + " = " + evaluated;
                    constTable.put(target, String.valueOf(evaluated));
                }
            } else {
                // If it's a simple assignment like x = 10 or t1 = x
                Matcher assignMatcher = assignPattern.matcher(trimmed);
                if (assignMatcher.matches()) {
                    String target = assignMatcher.group(1);
                    String val = assignMatcher.group(2).trim();
                    if (isConstant(val)) {
                        constTable.put(target, val);
                    } else {
                        constTable.remove(target);
                    }
                }
            }

            optimized.add(trimmed);
        }

        return optimized;
    }

    private List<String> eliminateDeadTemporaries(List<String> instructions) {
        // Collect all variables/temps read on the right-hand side or in conditions/params
        Set<String> used = new HashSet<>();
        for (String line : instructions) {
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                tokenizeUses(parts[1], used);
            } else {
                tokenizeUses(line, used);
            }
        }

        List<String> clean = new ArrayList<>();
        for (String line : instructions) {
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String left = parts[0].trim();
                // If an internal temporary t1, t2 was folded and never referenced again, eliminate it
                if (left.matches("^t\\d+$") && !used.contains(left)) {
                    continue; // Skip dead temporary instruction
                }
            }
            clean.add(line);
        }
        return clean;
    }

    private void tokenizeUses(String expr, Set<String> used) {
        Matcher m = Pattern.compile("[a-zA-Z0-9_\u0980-\u09FF]+").matcher(expr);
        while (m.find()) {
            used.add(m.group());
        }
    }

    private String replaceWord(String line, String target, String replacement) {
        // Only replace tokens appearing on the right side of the assignment
        int eqIdx = line.indexOf('=');
        if (eqIdx == -1) {
            return line.replaceAll("\\b" + Pattern.quote(target) + "\\b", replacement);
        }
        String left = line.substring(0, eqIdx + 1);
        String right = line.substring(eqIdx + 1);
        return left + right.replaceAll("\\b" + Pattern.quote(target) + "\\b", replacement);
    }

    private boolean isConstant(String val) {
        return val.matches("^-?\\d+$") || val.equals("true") || val.equals("false");
    }

    private Long evaluate(long left, String op, long right) {
        switch (op) {
            case "+": return left + right;
            case "-": return left - right;
            case "*": return left * right;
            case "/": return right != 0 ? left / right : null;
            case "%": return right != 0 ? left % right : null;
            default: return null;
        }
    }
}