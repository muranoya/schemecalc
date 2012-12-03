package schemecalc;

public class SNumeric extends SAtom
{
	private int num;
	public int getNum() { return this.num; }
	public void setNum(int num) { this.num = num; }
	
	public SNumeric add(SNumeric addNumeric) { return new SNumeric(this.getNum() + addNumeric.getNum()); }
	public SNumeric sub(SNumeric subNumeric) { return new SNumeric(this.getNum() - subNumeric.getNum()); }
	public SNumeric mul(SNumeric mulNumeric) { return new SNumeric(this.getNum() * mulNumeric.getNum()); }
	public SNumeric div(SNumeric divNumeric) { return new SNumeric(this.getNum() / divNumeric.getNum()); }

	public SBool eq(SNumeric eqNumeric) { return new SBool(eqNumeric.getNum() == this.getNum()); }
	public SBool great(SNumeric right) { return new SBool(this.getNum() < right.getNum()); }
	public SBool less(SNumeric right) { return new SBool(this.getNum() > right.getNum()); }
	
	@Override
	public String toString() { return String.valueOf(this.getNum()); }
	
	public SNumeric(int num)
	{
		this.setNum(num);
		this.setKind(AtomKind.Numeric);
	}
	public SNumeric()
	{
		this.setNum(0);
		this.setKind(AtomKind.Numeric);
	}
}
