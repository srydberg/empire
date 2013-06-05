package empire.lang;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Compiler
{
	private final InputStream input;
	private final OutputStream output;
	private final Map<String,Object> properties;
	
	public Compiler( InputStream input, OutputStream output, Map<String, Object> properties ) {
		this.input = input;
		this.output = output;
		this.properties = properties;
	}
	
	public void compile() throws IOException {
		final ANTLRInputStream input;
		final Writer writer;

		input = new ANTLRInputStream( new BufferedInputStream( this.input ) );
		writer = new BufferedWriter( new OutputStreamWriter( this.output ) );

		EmpireLexer lexer = new EmpireLexer( input );
		CommonTokenStream tokens = new CommonTokenStream( lexer );

		EmpireParser parser = new EmpireParser(tokens);
		ParseTree tree = parser.prog();
		
		EmpireBaseListener listener = new EmpireBaseListener() {
			Map<String, String> bindings = new HashMap<>();
			public void exitBinding( EmpireParser.BindingContext ctx ) {
				String property = ctx.Identifier(1).getText();
				
				if( !properties.containsKey( property ) ) {
					return;
				}
				
				Object value = properties.get( property );
				bindings.put(ctx.Identifier(0).getText(), value.toString());
			}
			
			String regex;
			public void exitLiteral(empire.lang.EmpireParser.LiteralContext ctx) {
				TerminalNode identifier = ctx.Identifier();
				TerminalNode regexLiteral = ctx.RegexLiteral();
				
				if(identifier != null) {
					regex = identifier.getText();
				}
				else
				if (regexLiteral != null) {
					regex = regexLiteral.getText();
					regex = regex.substring(2, regex.length()-1);
				}
			};
			
			Map<String, String> conditions = new HashMap<>();
			public void exitCondition(empire.lang.EmpireParser.ConditionContext ctx) {
				conditions.put(ctx.Identifier().getText(), regex);
				regex=null;
			};

			String output;
			public void exitOutputBlock(EmpireParser.OutputBlockContext ctx) {
				String text = ctx.OutputBlock().getText();
				text = text.substring(1, text.length()-1);
				output = text.trim();
			}

			public void exitStatement(EmpireParser.StatementContext ctx) {
				boolean conditionsValid = true;
				for(String key:conditions.keySet()) {
					String value = conditions.get(key);
					
					if(!bindings.containsKey( key )) {
						conditionsValid = false;
						break;
					}

					String boundValue = bindings.get(key);
					if(!Pattern.matches(value, boundValue)) {
						conditionsValid = false;
						break;
					}
				}
				
				if(conditionsValid) {
					try	{
						writer.write( output );
						writer.write( '\n' );
					} catch ( IOException e ) {
						throw new RuntimeException( e );
					}
				}
				
				conditions.clear();
				properties.clear();
			}
		};

		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk( listener, tree );
		writer.flush();
	}
}
