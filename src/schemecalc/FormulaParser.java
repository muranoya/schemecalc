package schemecalc;

import java.io.StringReader;
import java.util.Stack;

public class FormulaParser
{
	private Token token;
	private Scanner scanner;
	private Stack<SNumber> stack;
	private SInterpreter interpreter = null;
	
	public SNumber calcFormula(String exp, SInterpreter interpreter)
	{
		this.interpreter = interpreter;
		Util.resetErrorNum();
		scanner = new Scanner(new StringReader(exp));
		token = scanner.nextToken();
		if (token.getKind() == TokenKind.EofTkn)
			return new SNumber(0);
		statement();
		if (Util.occuredError())
			return new SNumber(0);
		
		return stack.pop();
	}
	
	private void statement()
	{
		expression();
		checkToken(TokenKind.EofTkn);
	}
	private void expression()
	{
		TokenKind op;
		
		term();
		while (token.getKind() == TokenKind.Plus || token.getKind() == TokenKind.Minus || isMinusNumber(token))
		{
			if (isMinusNumber(token))
			{
				op = TokenKind.Plus;
			}
			else
			{
				op = token.getKind();
				token = scanner.nextToken();
			}
			term();
			operate(op);
		}
	}
	private void term()
	{
		TokenKind op;
		
		factor();
		while (token.getKind() == TokenKind.Mul || token.getKind() == TokenKind.Divi || token.getKind() == TokenKind.Mod)
		{
			op = token.getKind();
			token = scanner.nextToken();
			factor();
			operate(op);
		}
	}
	private void factor()
	{
		switch (token.getKind())
		{
		case IntNum:
			stack.push(new SNumber(token.getIntVal()));
			break;
		case DoubleNum:
			stack.push(new SNumber(token.getDoubleVal()));
			break;
		case Lparen:
			token = scanner.nextToken();
			if (token.getKind() == TokenKind.DoubleNum || token.getKind() == TokenKind.IntNum)
			{
				expression();
				checkToken(TokenKind.Rparen);
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				int paren = 1;
				sb.append("(");
				sb.append(token.getText());
				if (token.getKind() == TokenKind.Lparen)
					paren++;
				Token tk;
				for (; paren != 0;)
				{
					tk = scanner.nextToken();
					sb.append(" " + tk.getText() + " ");
					if (tk.getKind() == TokenKind.Lparen)
						paren++;
					else if (tk.getKind() == TokenKind.Rparen)
						paren--;
				}
				SParser parser = new SParser();
				SAtom result = interpreter.eval(parser.parse(sb.toString()).get(0));
				if (result.getKind() != AtomKind.Numeric)
					Util.OnError("required number.");
				else
					stack.push((SNumber)result);
			}
			break;
		default:
			Util.OnError("Invalid formula.");
			break;
		}
		token = scanner.nextToken();
	}
	private void checkToken(TokenKind kind) { if (token.getKind() != kind) Util.OnError("Invalid Token."); }
	
	private boolean isMinusNumber(Token tk)
	{
		return (tk.getKind() == TokenKind.IntNum && tk.getIntVal() < 0) || (tk.getKind() == TokenKind.DoubleNum && tk.getDoubleVal() < 0);
	}
	
 	private void operate(TokenKind kind)
	{
		SNumber d1 = stack.pop();
		SNumber d2 = stack.pop();
		
		if (kind == TokenKind.Divi && d2.eq(new SNumber(0)).getBool())
		{
			Util.OnError("Can't divide by zero.");
			return ;
		}
		switch (kind)
		{
		case Plus:
			stack.push(d1.add(d2));
			break;
		case Minus:
			stack.push(d2.sub(d1));
			break;
		case Mul:
			stack.push(d1.mul(d2));
			break;
		case Divi:
			stack.push(d2.div(d1));
			break;
		case Mod:
			stack.push(d2.mod(d1));
		default:
			Util.OnError("unknown operator.", kind.toString());
			break;
		}
	}
	
	public FormulaParser()
	{
		this.stack = new Stack<SNumber>();
	}
}
