package schemecalc;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SParser
{
	private Scanner scanner;

	public List<SAtom> parse(String exp) { return parse(new StringReader(exp)); }
	public List<SAtom> parse(Reader reader)
	{
		scanner = new Scanner(reader);
		List<SAtom> retlist = new ArrayList<SAtom>();
		
		for (;;)
		{
			retlist.add(parseAtom());
			if (scanner.peekToken().getKind() == TokenKind.EofTkn)
				break;
		}
		return retlist;
	}
	
	private SCons parseList()
	{
		SCons ret = new SCons();
		SCons cons = ret;
		for (;;)
		{
			cons.setCar(parseAtom());
			if (scanner.peekToken().getKind() == TokenKind.Rparen)
			{
				cons.setCdr(SNil.nil);
				scanner.nextToken();
				break;
			}
			else if (scanner.peekToken().getKind() == TokenKind.Period)
			{
				scanner.nextToken();
				cons.setCdr(parseAtom());
				scanner.nextToken();
				break;
			}
			else
			{
				SCons newCons = new SCons();
				cons.setCdr(newCons);
				cons = newCons;
			}
		}
		return ret;
	}
	
	private SAtom parseAtom()
	{
		Token tk = scanner.nextToken();
		SAtom ret = null;
		switch (tk.getKind())
		{
		case Ident:
			ret = new SAtom(AtomKind.Symbol, tk.getText());
			break;
		case IntNum:
			ret = new SNumber(tk.getIntVal());
			break;
		case DoubleNum:
			ret = new SNumber(tk.getDoubleVal());
			break;
		case String:
			ret = new SAtom(AtomKind.String, tk.getText());
			break;
		case Char:
			ret = new SAtom(AtomKind.Char, tk.getText());
			break;
		case Plus:
		case Minus:
		case Mul:
		case Mod:
		case Divi:
			ret = new SAtom(AtomKind.Symbol, tk.getText());
			break;
		case True:
		case False:
			ret = new SBool(tk.getKind() == TokenKind.True ? true : false);
			break;
		case Lparen:
			if (scanner.peekToken().getKind() == TokenKind.Rparen)
			{
				scanner.nextToken();
				ret = SNil.nil;
			}
			else
			{
				ret = parseList();
			}
			break;
		case Apostrophe:
			ret = new SCons(new SAtom(AtomKind.Symbol, "quote"), new SCons(parseAtom(), SNil.nil));
			break;
		default:
			Util.OnError("Couldn't be interpreted.");
			return null;
		}
		return ret;
	}
	
	public SParser()
	{
		this.scanner = null;
	}
}
