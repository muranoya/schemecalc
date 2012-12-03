package schemecalc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class Scanner
{
	private PushbackReader reader;
	
	private int processLine = 0;
	private boolean isuseList;
	private char quote_ch = EOF;
	
	private Token peektk = null;
	
	private static Keyword[] keyTable;
	private static TokenKind[] chTable;
	private static char[] spaceTable;
	private static Set<Character> usingIdentMark;
	private static final char EOF = '\0';
	
	public Token nextToken()
	{
		if (peektk != null)
		{
			Token retTk = peektk;
			peektk = null;
			return retTk;
		}
		
		Token token = new Token(TokenKind.NulKind, "", 0);
		char ch;
		
		while (isSpace(ch = nextChar()));
		
		if (ch == EOF)
		{
			token.setKind(TokenKind.EofTkn);
			isuseList = true;
			return token;
		}
		
		switch (chTable[ch])
		{
		case Letter:
			StringBuilder sb = new StringBuilder();
			for (; chTable[ch] == TokenKind.Letter || chTable[ch] == TokenKind.Digit || usingIdentMark.contains(ch); ch = nextChar())
			{
				sb.append(ch);
				char c = toChar(peek());
				if (chTable[c] != TokenKind.Letter && chTable[c] != TokenKind.Digit && !usingIdentMark.contains(c))
					break;
			}
			token.setText(sb.toString());
			break;
		case Digit:
			token = setNumber(ch);
			break;
		case Minus:
			if (chTable[toChar(peek())] == TokenKind.Digit)
			{
				int mulnum = -1;
				token = setNumber(toChar(read()));
				if (token.getKind() == TokenKind.DoubleNum)
				{
					token.setDoubleVal(token.getDoubleVal() * mulnum);
					token.setText(String.valueOf(token.getDoubleVal()));
				}
				else if (token.getKind() == TokenKind.IntNum)
				{
					token.setIntVal(token.getIntVal() * mulnum);
					token.setText(String.valueOf(token.getIntVal()));
				}
			}
			else
			{
				if (chTable[ch] == TokenKind.Plus)
					token = new Token(TokenKind.Plus, "+", 0);
				else
					token = new Token(TokenKind.Minus, "-", 0);
			}
			break;
		case DblQ:
			StringBuilder str = new StringBuilder();
			while ((ch = nextChar()) != EOF && !isReturn(ch) && ch != '"')
			{
				if (ch == '\\' && (ch = nextChar()) == 'n') ch = '\n';
				str.append(ch);
			}
			if (ch != '"') Util.OnError("\"was not closed.");
			token.setKind(TokenKind.String);
			token.setText(str.toString());
			break;
		case Sharp:
			String stk = String.valueOf(ch) + String.valueOf(toChar(peek()));
			if (stk.equals("#t") || stk.equals("#f"))
			{
				read();
				token.setKind(stk.equals("#t") ? TokenKind.True : TokenKind.False);
				token.setText(stk);
			}
			else if (stk.equals("#??"))
			{
				read();
				char c = toChar(read());
				token.setKind(TokenKind.Char);
				token.setText(String.valueOf(c));
			}
			break;
		default:
			token.setText(String.valueOf(ch));
			break;
		}
		if (token.getKind() == TokenKind.NulKind) token = setKind(token);
		if (token.getKind() == TokenKind.Others) Util.OnError("Invalid Token.", token.getText());
		if (token.getKind() == TokenKind.EofTkn) isuseList = true;
		
		return token;
	}
	
	public Token peekToken()
	{
		peektk = nextToken();
		return peektk;
	}
	
	private char nextChar()
	{
		char ch = toChar(read());
		if (ch == EOF) return EOF;
		
		if (quote_ch != EOF)
		{
			if (ch == quote_ch)
			{
				quote_ch = EOF;
			}
			else if (isReturn(ch))
			{
				processLine++;
				quote_ch = EOF;
			}
			return ch;
		}
		
		if (isReturn(ch))
		{
			processLine++;
			return ch;
		}
		
		if (ch == '\"' || ch == '\'')
		{
			quote_ch = ch;
		}
		else if (ch == ';') // when comment
		{
			while ((ch = toChar(read())) != EOF && !isReturn(ch));
		}
		
		return ch;
	}
	
	private Token setNumber(char ch)
	{
		Token ret;
		if (ch == '0')
			switch (toChar(peek()))
			{
			case 'x':
				read();
				ret = setNum16();
				break;
			case '.':
				ret = setNum10(ch);
				break;
			default:
				ret = setNum8(ch);
				break;
			}
		else
			ret = setNum10(ch);
		return ret;
	}
	private Token setNum10(char ch)
	{
		StringBuilder num = new StringBuilder();
		
		while (chTable[ch] == TokenKind.Digit)
		{
			num.append(ch);
			if (chTable[toChar(peek())] != TokenKind.Digit)
				break;
			ch = nextChar();
		}
		if (chTable[toChar(peek())] == TokenKind.Period)
		{
			num.append(nextChar());
			while (chTable[ch = toChar(nextChar())] == TokenKind.Digit)
			{
				num.append(ch);
				if (chTable[toChar(peek())] != TokenKind.Digit)
					break;
			}
			return new Token(TokenKind.DoubleNum, num.toString(), Double.parseDouble(num.toString()));
		}
		else
		{
			return new Token(TokenKind.IntNum, num.toString(), Integer.parseInt(num.toString()));
		}
	}
	private Token setNum16()
	{
		int num;
		char ch = nextChar();
		for (num = 0; is16Num(ch); ch = nextChar())
		{
			num = num * 16 + get16Num(ch);
			if (!is16Num(toChar(peek())))
				break;
		}
		return new Token(TokenKind.IntNum, String.valueOf(num), num);
	}
	private Token setNum8(char ch)
	{
		int num;
		for (num = 0; is8Num(ch); ch = nextChar())
		{
			num = num * 8 + ch - '0';
			if (!is8Num(toChar(peek())))
				break;
		}
		return new Token(TokenKind.IntNum, String.valueOf(num), num);
	}
	private Token setKind(Token token)
	{
		token.setKind(TokenKind.Others);
		
		for (int i = 0; i < keyTable.length; i++)
		{
			if (keyTable[i].getKeyname().equals(token.getText()))
			{
				token.setKind(keyTable[i].getKind());
				return token;
			}
		}
		
		char ch = token.getText().toCharArray()[0];
		if (chTable[ch] == TokenKind.Letter)
			token.setKind(TokenKind.Ident);
		else if (chTable[ch] == TokenKind.Digit)
			token.setKind(TokenKind.IntNum);
		return token;
	}
	public Token checkNextToken(Token token, TokenKind kind)
	{
		if (token.getKind() == kind)
			return nextToken();
		else
		{
			Util.OnError(String.format("There is no %s in front of the %s", kind, token.getText()));
			return token;
		}
	}
	
	private static boolean is8Num(char ch) { return '0' <= ch && ch <= '9'; }
	private static boolean is16Num(char ch) { return ('0' <= ch && ch <= '9') || ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z'); }
	private static int get16Num(char ch)
	{
		if ('0' <= ch && ch <= '9')
			return ch - '0';
		else if ('a' <= ch && ch <= 'f')
			return ch - 'a' + 10;
		else if ('A' <= ch && ch <= 'F')
			return ch - 'A' + 10;
		return 0;
	}
	private static boolean isSpace(char c)
	{
		for (int i = 0; i < spaceTable.length; i++)
			if (spaceTable[i]== c) return true;
		return false;
	}
	private static char toChar(int x)
	{
		if (x == 65279) //BOM
			return ' ';
		if (x < 0)
			return EOF;
		else
			return Character.toChars(x)[0];
	}
	
	private boolean isReturn(char c)
	{
		if (c == '\r')
		{
			if (toChar(peek()) == '\n') read();
			return true;
		}
		else if (c == '\n')
			return true;
		return false;
	}
	
	private int peek()
	{
		int ret = read();
		if (ret < 0)
			ret = EOF;
		try
		{
			reader.unread(ret);
		}
		catch (IOException e)
		{
			return EOF;
		}
		return ret;
	}
	private int read()
	{
		try
		{
			return reader.read();
		}
		catch (IOException e)
		{
			return EOF;
		}
	}

	private void setTables()
	{
		keyTable = new Keyword[] {
            new Keyword("(", TokenKind.Lparen), new Keyword(")", TokenKind.Rparen),
            new Keyword(".", TokenKind.Period), new Keyword("'", TokenKind.Apostrophe),
            new Keyword("\"", TokenKind.DblQ), new Keyword("#", TokenKind.Sharp),
            new Keyword("+", TokenKind.Plus), new Keyword("-", TokenKind.Minus),
            new Keyword("*", TokenKind.Mul), new Keyword("/", TokenKind.Divi),
            new Keyword("%", TokenKind.Mod),
            new Keyword("#t", TokenKind.True), new Keyword("#f", TokenKind.False),
		};
		
		usingIdentMark = new HashSet<Character>();
		usingIdentMark.add('.');
		usingIdentMark.add('-');
		usingIdentMark.add('+');
		usingIdentMark.add('*');
		usingIdentMark.add('/');
		usingIdentMark.add('%');
		usingIdentMark.add('&');
		
        chTable = new TokenKind[256];
        int i;
        for (i = 0; i < chTable.length; i++)
        	chTable[i] = TokenKind.NulKind;
        for (i = '0'; i <= '9'; ++i) chTable[i] = TokenKind.Digit;
        for (i = 'A'; i <= 'Z'; ++i) chTable[i] = TokenKind.Letter;
        for (i = 'a'; i <= 'z'; ++i) chTable[i] = TokenKind.Letter;
        chTable['_'] = chTable['='] = chTable['?'] = chTable['!'] = TokenKind.Letter;
        chTable['<'] = chTable['>'] = chTable['$'] = TokenKind.Letter;
        chTable['('] = TokenKind.Lparen; chTable[')'] = TokenKind.Rparen;
        chTable['.'] = TokenKind.Period; chTable['\''] = TokenKind.Apostrophe;
        chTable['\"'] = TokenKind.DblQ; chTable['#'] = TokenKind.Sharp;
        chTable['+'] = TokenKind.Plus; chTable['-'] = TokenKind.Minus;
        chTable['%'] = TokenKind.Mod; chTable['/'] = TokenKind.Divi;
        chTable['*'] = TokenKind.Mul;
        spaceTable = new char[] { '\t', ' ', '\r', '\n', '\f' };
	}
	
	public Scanner(String fpath)
	{
		setTables();
		try
		{
			this.reader = new PushbackReader(new FileReader(fpath));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	public Scanner(Reader reader)
	{
		setTables();
		this.reader = new PushbackReader(reader);
	}
}
