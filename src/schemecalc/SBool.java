package schemecalc;

public class SBool extends SAtom
{
	private boolean bool;
	public boolean getBool() { return this.bool; }
	
	@Override
	public String getName() { return this.getBool() ? "#t" : "#f"; }
	
	@Override
	public String toString() { return this.getName(); }

	public SBool() { super(AtomKind.Bool, ""); }
	public SBool(boolean bool) { super(AtomKind.Bool, ""); this.bool = bool; }
}
