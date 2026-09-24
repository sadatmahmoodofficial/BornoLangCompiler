import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import java.util.ArrayList;
import java.util.List;

public class BanglaErrorListener extends BaseErrorListener {
    private final List<String> syntaxErrors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        
        // Translate or format common ANTLR error descriptions into clean diagnostic output
        String formattedMsg = "সিনট্যাক্স ত্রুটি (Syntax Error) [লাইন " + line + ":" + charPositionInLine + "] -> " + msg;
        syntaxErrors.add(formattedMsg);
    }

    public boolean hasErrors() {
        return !syntaxErrors.isEmpty();
    }

    public List<String> getSyntaxErrors() {
        return syntaxErrors;
    }
}