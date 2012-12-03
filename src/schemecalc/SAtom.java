package schemecalc;

public class SAtom
{
	private AtomKind kind;
	public AtomKind getKind() { return this.kind; }
	public void setKind(AtomKind kind) { this.kind = kind; }
	
	private String name;
	public String getName() { return this.name; }
	
	@Override
	public String toString() { return this.getName(); }
	@Override
	public int hashCode() { return this.name.hashCode(); }
	@Override
	public boolean equals(Object obj)
	{
		SAtom atom = (SAtom)obj;
		return this.name.equals(atom.getName());
	}
	
	public SAtom()
	{
		this.kind = AtomKind.NulKind;
		this.name = "";
	}
	public SAtom(AtomKind kind, String name)
	{
		this.kind = kind;
		this.name = name;
	}
}
