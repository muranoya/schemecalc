package schemecalc;

import java.util.HashMap;
import java.util.Map;

public class SFrame
{
	private SFrame pointer = null;
	private Map<SAtom, SAtom> list = new HashMap<SAtom, SAtom>();

	public SFrame getPointer() { return this.pointer; }
	
	private SAtom search(SAtom sym)
	{
		return list.get(sym);
	}
	
	public SAtom get(SAtom sym)
	{
		SAtom symbol = search(sym);
		if (symbol != null)
			return symbol;
		if (pointer != null)
			return pointer.get(sym);
		return null;
	}
	
	public SAtom add(SAtom name, SAtom sym)
	{
		SAtom symbol = search(name);
		if (symbol != null)
			list.remove(symbol);
		list.put(name, sym);
		return name;
	}
	
	public SFrame(SFrame pointer)
	{
		this.pointer = pointer;
	}
}
