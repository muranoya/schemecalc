package schemecalc;

public class SNil extends SAtom
{
	public static final SNil nil = new SNil();
	
	@Override
	public String toString() { return "()"; }
	
	private SNil() { super(AtomKind.Nil, "nil"); }
}
