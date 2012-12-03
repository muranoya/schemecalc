package schemecalc;

public class Keyword
{
	private String keyname;
	public String getKeyname() { return this.keyname; }
	public void setKeyname(String keyname) { this.keyname = keyname; }
	
	private TokenKind kind;
	public TokenKind getKind() { return this.kind; }
	public void setKind(TokenKind kind) { this.kind = kind;}
	
	@Override
	public String toString() { return String.format("%s %s", this.keyname, this.kind.toString()); }
	
	public Keyword(String keyname, TokenKind kind)
	{
		this.keyname = keyname;
		this.kind = kind;
	}
}
