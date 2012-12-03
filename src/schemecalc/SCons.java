package schemecalc;

public class SCons extends SAtom
{
	private SAtom car = SNil.nil;
	public SAtom getCar() { return this.car; }
	public void setCar(SAtom atom) { this.car = atom; }
	
	private SAtom cdr = SNil.nil;
	public SAtom getCdr() { return this.cdr; }
	public void setCdr(SAtom atom) { this.cdr = atom; }
	
	@Override
	public String toString() { return getListString(this); }
	private String getListString(SCons cons)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		
		SCons temp = cons;
		for (;;)
		{
			if (temp.getCar().getKind() == AtomKind.Cons)
				sb.append(getListString((SCons)temp.getCar()));
			else
				sb.append(temp.getCar());
			
			if (temp.getCdr().getKind() != AtomKind.Cons)
			{
				if (temp.getCdr().getKind() != AtomKind.Nil)
					sb.append(" . " + temp.getCdr());
				break;
			}
			else
			{
				temp = (SCons)temp.getCdr();
			}
			sb.append(" ");
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	public SCons()
	{
		super(AtomKind.Cons, "");
		this.setCar(SNil.nil);
		this.setCdr(SNil.nil);
	}
	public SCons(SAtom car, SAtom cdr)
	{
		super(AtomKind.Cons, "");
		this.setCar(car);
		this.setCdr(cdr);
	}
}
