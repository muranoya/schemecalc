package schemecalc;

public class Token
{
	private TokenKind kind;
	public TokenKind getKind() { return this.kind; }
	public void setKind(TokenKind kind) { this.kind = kind; }
	
	private String text;
	public String getText() { return this.text; }
	public void setText(String text) { this.text = text; }
	
	private int intVal;
	public int getIntVal() { return this.intVal; }
	public void setIntVal(int intVal) { this.intVal = intVal; }
	
	private double doubleVal;
	public double getDoubleVal() { return this.doubleVal; }
	public void setDoubleVal(double doubleVal) { this.doubleVal = doubleVal; }
	
	@Override
	public String toString() { return String.format("%s %s", this.kind.toString(), this.text); }
	
	public Token(TokenKind kind, String text, int intVal)
	{
		this.kind = kind;
		this.text = text;
		this.intVal = intVal;
		this.doubleVal = 0.0;
	}
	public Token(TokenKind kind, String text, double doubleVal)
	{
		this.kind = kind;
		this.text = text;
		this.intVal = 0;
		this.doubleVal = doubleVal;
	}
}
