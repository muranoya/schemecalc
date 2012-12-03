package schemecalc;

public class SNumber extends SAtom
{
	private int numi;
	private double numd;
	
	private SNumberType type;
	public SNumberType getType() { return this.type; }
	
	public SNumber add(SNumber op)
	{
		if (this.type == SNumberType.Integer && op.type == SNumberType.Integer)
			return new SNumber(this.numi + op.numi);
		else if (this.type == SNumberType.Integer && op.type == SNumberType.Double)
			return new SNumber(((double)this.numi) + op.numd);
		else if (this.type == SNumberType.Double && op.type == SNumberType.Integer)
			return new SNumber(this.numd + ((double)op.numi));
		else if (this.type == SNumberType.Double && op.type == SNumberType.Double)
			return new SNumber(this.numd + op.numd);
		Util.OnError("Unknown number type.");
		return null;
	}
	public SNumber sub(SNumber op)
	{
		if (this.type == SNumberType.Integer && op.type == SNumberType.Integer)
			return new SNumber(this.numi - op.numi);
		else if (this.type == SNumberType.Integer && op.type == SNumberType.Double)
			return new SNumber(((double)this.numi) - op.numd);
		else if (this.type == SNumberType.Double && op.type == SNumberType.Integer)
			return new SNumber(this.numd - ((double)op.numi));
		else if (this.type == SNumberType.Double && op.type == SNumberType.Double)
			return new SNumber(this.numd - op.numd);
		Util.OnError("Unknown number type.");
		return null;
	}
	public SNumber mul(SNumber op)
	{
		if (this.type == SNumberType.Integer && op.type == SNumberType.Integer)
			return new SNumber(this.numi * op.numi);
		else if (this.type == SNumberType.Integer && op.type == SNumberType.Double)
			return new SNumber(((double)this.numi) * op.numd);
		else if (this.type == SNumberType.Double && op.type == SNumberType.Integer)
			return new SNumber(this.numd * ((double)op.numi));
		else if (this.type == SNumberType.Double && op.type == SNumberType.Double)
			return new SNumber(this.numd * op.numd);
		Util.OnError("Unknown number type.");
		return null;
	}
	public SNumber div(SNumber op)
	{
		if (this.type == SNumberType.Integer && op.type == SNumberType.Integer)
			return new SNumber(this.numi / op.numi);
		else if (this.type == SNumberType.Integer && op.type == SNumberType.Double)
			return new SNumber(((double)this.numi) / op.numd);
		else if (this.type == SNumberType.Double && op.type == SNumberType.Integer)
			return new SNumber(this.numd / ((double)op.numi));
		else if (this.type == SNumberType.Double && op.type == SNumberType.Double)
			return new SNumber(this.numd / op.numd);
		Util.OnError("Unknown number type.");
		return null;
	}
	public SNumber mod(SNumber op)
	{
		if (this.type == SNumberType.Integer && op.type == SNumberType.Integer)
			return new SNumber(this.numi % op.numi);
		Util.OnError("integer required.");
		return null;
	}

	public SBool eq(SNumber op)
	{
		if (this.type == SNumberType.Integer && op.type == SNumberType.Integer)
			return new SBool(this.numi == op.numi);
		if (this.type == SNumberType.Integer && op.type == SNumberType.Double)
			return new SBool(this.numi == op.numd);
		else if (this.type == SNumberType.Double && op.type == SNumberType.Integer)
			return new SBool(this.numd == ((double)op.numi));
		else if (this.type == SNumberType.Double && op.type == SNumberType.Double)
			return new SBool(this.numd == op.numd);
		Util.OnError("Unknown number type.");
		return null;
	}
	public SBool great(SNumber right)
	{
		if (this.type == SNumberType.Integer && right.type == SNumberType.Integer)
			return new SBool(this.numi < right.numi);
		if (this.type == SNumberType.Integer && right.type == SNumberType.Double)
			return new SBool(this.numi < right.numd);
		else if (this.type == SNumberType.Double && right.type == SNumberType.Integer)
			return new SBool(this.numd < ((double)right.numi));
		else if (this.type == SNumberType.Double && right.type == SNumberType.Double)
			return new SBool(this.numd < right.numd);
		Util.OnError("Unknown number type.");
		return null;
	}
	public SBool less(SNumber right)
	{
		if (this.type == SNumberType.Integer && right.type == SNumberType.Integer)
			return new SBool(this.numi > right.numi);
		if (this.type == SNumberType.Integer && right.type == SNumberType.Double)
			return new SBool(this.numi > right.numd);
		else if (this.type == SNumberType.Double && right.type == SNumberType.Integer)
			return new SBool(this.numd > ((double)right.numi));
		else if (this.type == SNumberType.Double && right.type == SNumberType.Double)
			return new SBool(this.numd > right.numd);
		Util.OnError("Unknown number type.");
		return null;
	}
	
	public static SNumber exp(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.exp(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.exp((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber log(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.log(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.log((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber sin(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.sin(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.sin((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber cos(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.cos(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.cos((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber tan(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.tan(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.tan((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber asin(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.asin(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.asin((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber acos(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.acos(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.acos((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber atan(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.atan(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.atan((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber atan(SNumber op1, SNumber op2)
	{
		if (op1.type == SNumberType.Integer && op2.type == SNumberType.Integer)
			return new SNumber(Math.atan2((double)op1.numi, (double)op2.numi));
		else if (op1.type == SNumberType.Integer && op2.type == SNumberType.Double)
			return new SNumber(Math.atan2((double)op1.numi, op2.numd));
		else if (op1.type == SNumberType.Double && op2.type == SNumberType.Integer)
			return new SNumber(Math.atan2(op1.numd, (double)op2.numi));
		else if (op1.type == SNumberType.Double && op2.type == SNumberType.Double)
			return new SNumber(Math.atan2(op1.numd, op2.numd));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber sqrt(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.sqrt(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.sqrt((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber expt(SNumber op1, SNumber op2)
	{
		if (op1.getType() == SNumberType.Integer && op2.getType() == SNumberType.Integer)
			return new SNumber(Math.pow((double)op1.numi, (double)op2.numi));
		else if (op1.getType() == SNumberType.Integer && op2.getType() == SNumberType.Double)
			return new SNumber(Math.pow((double)op1.numi, op2.numd));
		else if (op1.getType() == SNumberType.Double && op2.getType() == SNumberType.Integer)
			return new SNumber(Math.pow(op1.numd, (double)op2.numi));
		else if (op1.getType() == SNumberType.Double && op2.getType() == SNumberType.Double)
			return new SNumber(Math.pow(op1.numd, op2.numd));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber floor(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.floor(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.floor((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	public static SNumber ceiling(SNumber op)
	{
		if (op.getType() == SNumberType.Double)
			return new SNumber(Math.ceil(op.numd));
		else if (op.getType() == SNumberType.Integer)
			return new SNumber(Math.ceil((double)op.numi));
		Util.OnError("Unknown number type.");
		return null;
	}
	
	@Override
	public String toString()
	{
		if (this.type == SNumberType.Integer)
			return String.valueOf(this.numi);
		else if (this.type == SNumberType.Double)
			return String.valueOf(this.numd);
		return "";
	}
	
	public SNumber(double num)
	{
		super(AtomKind.Numeric, String.valueOf(num));
		this.numd = num;
		this.type = SNumberType.Double;
	}
	public SNumber(int num)
	{
		super(AtomKind.Numeric, String.valueOf(num));
		this.numi = num;
		this.type = SNumberType.Integer;
	}
	public SNumber()
	{
		super(AtomKind.Numeric, "0");
		this.numi = 0;
		this.type = SNumberType.Integer;
	}
}
