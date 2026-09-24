import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, String> symbols = new HashMap<>();
    private final SymbolTable parent;

    public SymbolTable() {
        this(null);
    }

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public boolean declare(String name, String type) {
        if (symbols.containsKey(name)) {
            return false; // Already declared in this current scope
        }
        symbols.put(name, type);
        return true;
    }

    public String lookup(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null; // Undeclared variable
    }

    public boolean exists(String name) {
        return lookup(name) != null;
    }

    public SymbolTable getParent() {
        return parent;
    }
}