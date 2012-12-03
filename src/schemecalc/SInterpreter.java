package schemecalc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class SInterpreter
{
	private SFrame globalenv = new SFrame(null);
	private Set<String> builtinList;
	
	public SAtom eval(SAtom atom, SFrame env)
	{
		if (atom.getKind() == AtomKind.Cons)
		{
			SCons cons = (SCons)atom;
			if (cons.getCar().getKind() == AtomKind.Symbol)
				return call(env, cons);
			else if (cons.getCar().getKind() == AtomKind.Cons)
			{
				SAtom fname = ((SCons)cons.getCar()).getCar();
				if (fname.getName().equals("lambda"))
				{
					SCons lambdaBody = (SCons)((SCons)cons.getCar()).getCdr();
					return callFunction(lambdaBody, cons.getCdr(), fname, env);
				}
				else
				{
					SAtom sym = eval(cons.getCar(), env);
					SAtom newexp = new SCons(sym, cons.getCdr());
					return eval(newexp, env);
				}
			}
			else
				Util.OnError("Invalid expression.");
		}
		else
		{
			if (atom.getKind() == AtomKind.Symbol)
			{
				if (isBuiltinFunc(atom))
					return atom;
				SAtom sym = env.get(atom);
				if (sym == null)
				{
					Util.OnError("unbound variable : ", atom.getName());
					return null;
				}
				return sym.getKind() == AtomKind.Lambda ? atom : sym;
			}
			else
				return atom;
		}
		return null;
	}
	public SAtom eval(SAtom atom) { return eval(atom, globalenv); }
	
 	private SAtom call(SFrame env, SCons cons)
	{
		switch (cons.getCar().getName())
		{
		case "car":		return sysCar(cons, env);
		case "set-car!":return sysSetCarEM(cons, env);
		case "cdr":		return sysCdr(cons, env);
		case "set-cdr!":return sysSetCdrEM(cons, env);
		case "cons":	return sysCons(cons, env);
		case "list":	return sysList(cons, env);
		case "+":		return sysPlus(cons, env);
		case "-":		return sysMinus(cons, env);
		case "*":		return sysMulti(cons, env);
		case "/":		return sysDivi(cons, env);
		case "=":		return sysEqual(cons, env);
		case "<":		return sysGreat(cons, env);
		case "<=":		return sysGreatEq(cons, env);
		case ">":		return sysLess(cons, env);
		case ">=":		return sysLessEq(cons, env);
		case "let":		return sysLet(cons, env);
		case "procedure?":	return sysProcedureQ(cons, env);
		case "define":	return sysDefine(cons, env);
		case "lambda":	return sysLambda(cons);
		case "apply":	return sysApply(cons, env);
		case "not":		return sysNot(cons, env);
		case "and":		return sysAnd(cons, env);
		case "or":		return sysOr(cons, env);
		case "quote":	return sysQuote(cons);
		case "if":		return sysIf(cons, env);
		case "cond":	return sysCond(cons, env);
		case "else":	return new SBool(true);
		case "begin":	return sysBegin(cons, env);
		case "modulo":	return sysModulo(cons, env);
		case "exp":		return sysExp(cons, env);
		case "log":		return sysLog(cons, env);
		case "sin":		return sysSin(cons, env);
		case "cos":		return sysCos(cons, env);
		case "tan":		return sysTan(cons, env);
		case "asin":	return sysAsin(cons, env);
		case "acos":	return sysAcos(cons, env);
		case "atan":	return sysAtan(cons, env);
		case "sqrt":	return sysSqrt(cons, env);
		case "expt":	return sysExpt(cons, env);
		case "floor":	return sysFloor(cons, env);
		case "ceiling":	return sysCeiling(cons, env);
		case "pair?":	return sysPairQ(cons, env);
		case "boolean?":return sysBooleanQ(cons, env);
		case "list?":	return sysListQ(cons, env);
		case "null?":	return sysNullQ(cons, env);
		case "symbol?":	return sysSymbolQ(cons, env);
		case "char?":	return sysCharQ(cons, env);
		case "string?":	return sysStringQ(cons, env);
		case "number?":	return sysNumberQ(cons, env);
		default: return callFromSymbol(cons, env);
		}
	}
 	
 	private SAtom callFromSymbol(SCons cons, SFrame env)
 	{
 		SAtom fname = cons.getCar();
		SCons fnc = (SCons)env.get(fname);
		if (fnc == null || fnc.getKind() != AtomKind.Lambda)
			Util.OnError("Undefined symbol: ", fname.toString());
		else
			return callFunction(fnc, cons.getCdr(), fname, env);
		return null;
 	}
 	private SAtom callFunction(SCons fnc, SAtom args, SAtom fname, SFrame env)
 	{
 		SFrame newEnv = new SFrame(env);
		SAtom farg = fnc.getCar();
		SAtom carg = args;
		
		//apply arguments
		if (isVariableArg(farg)) //(lambda (a . b))
		{
			setVaribleArg(farg, (SCons)carg, newEnv);
		}
		else if (farg.getKind() == AtomKind.Nil) //(lambda ())
		{
			if (eval(carg, env).getKind() != AtomKind.Nil)
				Util.OnError("Number of arguments does not match: ", fname.toString());
		}
		else //(lambda (a b c))
		{
			for (;;)
			{
				if (farg.getKind() == AtomKind.Nil && carg.getKind() == AtomKind.Nil)
				{
					break;
				}
				else if (farg.getKind() == AtomKind.Nil || carg.getKind() == AtomKind.Nil)
				{
					Util.OnError("Number of arguments does not match: ", fname.toString());
					break;
				}
				newEnv.add(((SCons)farg).getCar(), eval(((SCons)carg).getCar(), env));
				farg = ((SCons)farg).getCdr();
				carg = ((SCons)carg).getCdr();
			}
		}
		
		//run function
		SAtom funcBody = ((SCons)fnc.getCdr()).getCar();
		SAtom funcRecur = ((SCons)fnc.getCdr()).getCdr();
		SAtom result = null;
		for (;;)
		{
			result = eval(funcBody, newEnv);
			if (funcRecur.getKind() == AtomKind.Nil)
				break;
			funcBody = ((SCons)funcRecur).getCar();
			funcRecur = ((SCons)funcRecur).getCdr();
		}
		return result;
 	}
 	private boolean isVariableArg(SAtom args)
 	{
 		if (args.getKind() != AtomKind.Cons)
 			return true;
 		for (;;)
 		{
 			args = ((SCons)args).getCdr();
 			if (args.getKind() == AtomKind.Nil)
 				return false;
 			if (args.getKind() != AtomKind.Cons)
 				return true;
 		}
 	}
 	private void setVaribleArg(SAtom funcArg, SCons callArg, SFrame env)
 	{
 		SFrame oldEnv = env.getPointer();
 		if (funcArg.getKind() != AtomKind.Cons)
 		{
 			SCons args = new SCons();
 			SCons temp = args;
 			for (;;)
 			{
 				temp.setCar(eval(callArg.getCar(), oldEnv));
 				if (callArg.getCdr().getKind() == AtomKind.Nil)
 				{
 					break;
 				}
 				callArg = (SCons)callArg.getCdr();
 				SCons tempCons = new SCons();
 				temp.setCdr(tempCons);
 				temp = tempCons;
 			}
 			env.add(funcArg, args);
 			return;
 		}
 		
 		//make args list
 		SCons list = new SCons();
 		SCons temp = list;
 		for (;;)
 		{
 			temp.setCar(eval(callArg.getCar(), oldEnv));
 			if (callArg.getCdr().getKind() == AtomKind.Nil)
 				break;
 			callArg = (SCons)callArg.getCdr();
 			SCons constemp = new SCons();
 			temp.setCdr(constemp);
 			temp = constemp;
		}
 		
 		//apply argments
 		SCons funcArgs = (SCons)funcArg;
 		for (;;)
 		{
 			env.add(funcArgs.getCar(), list.getCar());
 			if (funcArgs.getCdr().getKind() != AtomKind.Cons)
 			{
 				env.add(funcArgs.getCdr(), list.getCdr());
 				break;
 			}
 			funcArgs =(SCons)funcArgs.getCdr();
 			list = (SCons)list.getCdr();
 		}
	}
	
 	private boolean isBuiltinFunc(SAtom atom) { return builtinList.contains(atom.getName()); }
 	
 	private SAtom getUndefSymbol() { return new SAtom(AtomKind.Symbol, "undef"); }
 	
	private SAtom sysCar(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		cons = (SCons)eval(cons.getCar(), env);
		return cons.getCar();
	}
	private SAtom sysSetCarEM(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SCons op1 = (SCons)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		SAtom op2 = eval(cons.getCar(), env);
		op1.setCar(op2);
		return null;
	}
	private SAtom sysCdr(SCons cons, SFrame env)
	{
		cons =(SCons)cons.getCdr();
		cons = (SCons)eval(cons.getCar(), env);
		return cons.getCdr();
	}
	private SAtom sysSetCdrEM(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SCons op1 = (SCons)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		SAtom op2 = eval(cons.getCar(), env);
		op1.setCdr(op2);
		return null;
	}
	private SCons sysCons(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SAtom first = eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		SAtom last = eval(cons.getCar(), env);
		return new SCons(first, last);
	}
	private SCons sysList(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		
		SCons ret = new SCons();
		SCons temp = ret;
		for (;;)
		{
			temp.setCar(eval(cons.getCar(), env));
			if (cons.getCdr().getKind() == AtomKind.Nil)
			{
				temp.setCdr(SNil.nil);
				break;
			}
			SCons newCons = new SCons();
			temp.setCdr(newCons);
			temp = newCons;
			cons = (SCons)cons.getCdr();
		}
		return ret;
	}
	
	private SNumber sysPlus(SCons cons, SFrame env)
	{
		SNumber ret = new SNumber(0);
		
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SAtom atom = eval(cons.getCar(), env);
			ret = ret.add((SNumber)atom);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return ret;
	}
	private SNumber sysMinus(SCons cons, SFrame env)
	{
		SNumber ret;
		
		cons = (SCons)cons.getCdr();
		ret = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SAtom atom = eval(cons.getCar(), env);
			ret = ret.sub((SNumber)atom);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return ret;
	}
	private SNumber sysMulti(SCons cons, SFrame env)
	{
		SNumber ret = new SNumber(1);
		
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SAtom atom = eval(cons.getCar(), env);
			ret = ret.mul((SNumber)atom);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return ret;
	}
	private SNumber sysDivi(SCons cons, SFrame env)
	{
		SNumber ret;

		cons = (SCons)cons.getCdr();
		ret = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SAtom atom = eval(cons.getCar(), env);
			ret = ret.div((SNumber)atom);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return ret;
	}
	
	private SBool sysEqual(SCons cons, SFrame env)
	{
		SNumber first;
		
		cons = (SCons)cons.getCdr();
		first = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SAtom atom = eval(cons.getCar(), env);
			if (!first.eq((SNumber)atom).getBool())
				return new SBool(false);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return new SBool(true);
	}
	private SBool sysGreat(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SNumber left = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SNumber right = (SNumber)eval(cons.getCar(), env);
			if (left.great(right).getBool() == false)
				return new SBool(false);
			left = right;
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return new SBool(true);
	}
	private SBool sysGreatEq(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SNumber left = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SNumber right = (SNumber)eval(cons.getCar(), env);
			if (left.less(right).getBool())
				return new SBool(false);
			left = right;
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return new SBool(true);
	}
	private SBool sysLess(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SNumber left = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SNumber right = (SNumber)eval(cons.getCar(), env);
			if (left.less(right).getBool() == false)
				return new SBool(false);
			left = right;
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return new SBool(true);
	}
	private SBool sysLessEq(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SNumber left = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SNumber right = (SNumber)eval(cons.getCar(), env);
			if (left.great(right).getBool())
				return new SBool(false);
			left = right;
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return new SBool(true);
	}

	private SAtom sysLet(SCons cons, SFrame env)
	{
		SFrame newEnv = new SFrame(env);
		cons = (SCons)cons.getCdr();
		SCons letbody = (SCons)cons.getCar();
		SAtom letexp = cons.getCdr();
		SCons letpair = (SCons)letbody.getCar();
		for (;;)
		{
			SAtom letvalue = ((SCons)letpair.getCdr()).getCar();
			newEnv.add(letpair.getCar(), eval(letvalue, newEnv));
			if (letbody.getCdr().getKind() == AtomKind.Nil)
				break;
			letbody = (SCons)letbody.getCdr();
			letpair = (SCons)letbody.getCar();
		}
		
		if (letexp.getKind() == AtomKind.Nil)
			return getUndefSymbol();
		
		SCons exps = (SCons)letexp;
		SAtom ret;
		SAtom exp;
		for (;;)
		{
			exp = exps.getCar();
			ret = eval(exp, newEnv);
			if (exps.getCdr().getKind() == AtomKind.Nil)
				return ret;
			exps = (SCons)exps.getCdr();
		}
	}
	private SBool sysProcedureQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SAtom func_arg = cons.getCar();
		if (func_arg.getKind() == AtomKind.Cons)
		{
			if (((SCons)func_arg).getCar().getName().equals("quote"))
			{
				return new SBool(false);
			}
			else
			{
				SAtom proc = eval(func_arg, env);
				if (isBuiltinFunc(proc))
				{
					return new SBool(true);
				}
				else
				{
					return new SBool(env.get(proc).getKind() == AtomKind.Lambda);
				}
			}
		}
		else
		{
			if (isBuiltinFunc(func_arg))
			{
				return new SBool(true);
			}	
			else
			{
				return new SBool(env.get(func_arg).getKind() == AtomKind.Lambda);
			}
		}
	}
	private SAtom sysDefine(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SAtom name = cons.getCar();
		cons = (SCons)cons.getCdr();
		SAtom sym = eval(cons.getCar(), env);
		return env.add(name, sym);
	}
	private SAtom sysLambda(SCons cons)
	{
		cons = (SCons)cons.getCdr();
		cons.setKind(AtomKind.Lambda);
		return cons;
	}
	private SAtom sysApply(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SAtom proc = eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		
		SAtom args;
		Stack<SAtom> stack = new Stack<SAtom>();
		for (;;)
		{
			stack.push(eval(cons.getCar(), env));
			if (cons.getCdr().getKind() == AtomKind.Nil)
			{
				if (stack.peek().getKind() != AtomKind.Cons && stack.peek().getKind() != AtomKind.Nil)
				{
					Util.OnError("last argument is not a list.", "apply");
				}
				break;
			}
			cons = (SCons)cons.getCdr();
		}
		
		args = stack.pop();
		for (; !stack.empty();)
		{
			args = new SCons(stack.pop(), args);
		}
		
		return eval(new SCons(proc, args), env);
	}
	
	private SBool sysNot(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SBool b = (SBool)eval(cons.getCar(), env);
		return new SBool(!b.getBool());
	}
	private SBool sysAnd(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SBool atom = (SBool)eval(cons.getCar(), env);
			if (!atom.getBool())
				return new SBool(false);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return new SBool(true);
	}
	private SBool sysOr(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		for (;;)
		{
			SBool atom = (SBool)eval(cons.getCar(), env);
			if (atom.getBool())
				return new SBool(true);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break;
			else
				cons = (SCons)cons.getCdr();
		}
		return new SBool(false);
	}
	private SAtom sysQuote(SCons cons)
	{
		cons = (SCons)cons.getCdr();
		return cons.getCar();
	}
	private SAtom sysIf(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SBool result = (SBool)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		if (result.getBool())
		{
			return eval(cons.getCar(), env);
		}
		else
		{
			cons = (SCons)cons.getCdr();
			return eval(cons.getCar(), env);
		}
	}
	private SAtom sysCond(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		
		SCons clause = (SCons)cons.getCar();
		for (;;)
		{
			SBool result;
			if (clause.getCar().getKind() == AtomKind.Symbol && clause.getCar().getName().equals("else"))
				result = new SBool(true);
			else
				result = (SBool)eval(clause.getCar(), env);
			if (result.getBool())
				return eval(((SCons)clause.getCdr()).getCar(), env);
			if (cons.getCdr().getKind() != AtomKind.Cons)
				break;
			cons = (SCons)cons.getCdr();
			clause = (SCons)cons.getCar();
		}
		return null;
	}
	private SAtom sysBegin(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SAtom last;
		for (;;)
		{
			last = eval(cons.getCar(), env);
			if (cons.getCdr().getKind() == AtomKind.Nil)
				break; 
			cons = (SCons) cons.getCdr();
		}
		return last;
	}
	
	private SNumber sysModulo(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SNumber left = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		SNumber right = (SNumber)eval(cons.getCar(), env);
		return left.mod(right);
	}
	private SNumber sysExp(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.exp((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysLog(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.log((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysSin(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.sin((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysCos(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.cos((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysTan(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.tan((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysAsin(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.asin((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysAcos(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.acos((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysAtan(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SNumber op1 = (SNumber)eval(cons.getCar(), env);
		if (cons.getCdr().getKind() == AtomKind.Nil)
		{
			return SNumber.atan(op1);
		}
		cons = (SCons)cons.getCdr();
		SNumber op2 = (SNumber)eval(cons.getCar(), env);
		return SNumber.atan(op1, op2);
	}
	private SNumber sysSqrt(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.sqrt((SNumber)eval(cons.getCar(), env));
	}
	private SNumber sysExpt(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		SNumber left = (SNumber)eval(cons.getCar(), env);
		cons = (SCons)cons.getCdr();
		SNumber right = (SNumber)eval(cons.getCar(), env);
		return SNumber.expt(left, right);
	}
	private SNumber sysFloor(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.floor((SNumber)eval(cons.getCar(), env));		
	}
	private SNumber sysCeiling(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return SNumber.ceiling((SNumber)eval(cons.getCar(), env));
	}
	
 	private SBool sysPairQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return new SBool(eval(cons.getCar(), env).getKind() == AtomKind.Cons);
	}
	private SBool sysBooleanQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return new SBool(eval(cons.getCar(), env).getKind() == AtomKind.Bool);
	}
	private SBool sysListQ(SCons cons, SFrame env)
	{
		SAtom atom = eval(((SCons)cons.getCdr()).getCar(), env);
		if (atom.getKind() == AtomKind.Nil)
			return new SBool(true);
		for (;;)
		{
			if (atom.getKind() == AtomKind.Cons)
			{
				SCons temp = (SCons)atom;
				if (temp.getCdr().getKind() == AtomKind.Nil)
					return new SBool(true);
				else if (temp.getCdr().getKind() == AtomKind.Cons)
					atom = temp.getCdr();
				else
					break;
			}
			else
				break;
		}
		return new SBool(false);
	}
	private SBool sysNullQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return new SBool(eval(cons.getCar(), env).getKind() == AtomKind.Nil);
	}
	private SBool sysSymbolQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return new SBool(eval(cons.getCar(), env).getKind() == AtomKind.Symbol);
	}
	private SBool sysCharQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return new SBool(eval(cons.getCar(), env).getKind() == AtomKind.Char);
	}
	private SBool sysStringQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return new SBool(eval(cons.getCar(), env).getKind() == AtomKind.String);
	}
	private SBool sysNumberQ(SCons cons, SFrame env)
	{
		cons = (SCons)cons.getCdr();
		return new SBool(eval(cons.getCar(), env).getKind() == AtomKind.Numeric);
	}

 	private void setBuiltinList()
 	{
 		builtinList = new HashSet<>();
 		builtinList.add("car");
 		builtinList.add("set-car!");
 		builtinList.add("cdr");
 		builtinList.add("set-cdr!");
 		builtinList.add("cons");
 		builtinList.add("list");
 		builtinList.add("+");
 		builtinList.add("-");
 		builtinList.add("*");
 		builtinList.add("/");
 		builtinList.add("=");
 		builtinList.add("<");
 		builtinList.add("<=");
 		builtinList.add(">");
 		builtinList.add(">=");
 		builtinList.add("let");
 		builtinList.add("procedure?");
 		builtinList.add("define");
 		builtinList.add("lambda");
 		builtinList.add("not");
 		builtinList.add("and");
 		builtinList.add("or");
 		builtinList.add("quote");
 		builtinList.add("if");
 		builtinList.add("cond");
 		builtinList.add("begin");
 		builtinList.add("modulo");
 		builtinList.add("exp");
 		builtinList.add("log");
 		builtinList.add("sin");
 		builtinList.add("cos");
 		builtinList.add("tan");
 		builtinList.add("asin");
 		builtinList.add("acos");
 		builtinList.add("atan");
 		builtinList.add("sqrt");
 		builtinList.add("expt");
 		builtinList.add("floor");
 		builtinList.add("ceiling");
 		builtinList.add("pair?");
 		builtinList.add("boolean?");
 		builtinList.add("list?");
 		builtinList.add("null?");
 		builtinList.add("symbol?");
 		builtinList.add("char?");
 		builtinList.add("string?");
 		builtinList.add("number?");
 	}
 	private void eval_defLib(SParser parser, String str)
 	{
 		List<SAtom> list = parser.parse(str);
 		for (SAtom exp : list)
 		{
 			eval(exp);
 		}
 	}
	private void setLibrary()
	{
		SParser parser = new SParser();
		eval_defLib(parser, "(define zero? (lambda (z) (= 0 z)))");
		eval_defLib(parser, "(define positive? (lambda (x) (> x 0)))");
		eval_defLib(parser, "(define negative? (lambda (x) (> 0 x)))");
		eval_defLib(parser, "(define odd? (lambda (n) (not (even? n))))");
		eval_defLib(parser, "(define even? (lambda (n) (zero? (modulo n 2))))");
		eval_defLib(parser, "(define abs (lambda (x) (if (negative? x) (* -1 x) x)))");
		eval_defLib(parser, "(define max" +
								"(lambda (a . b)" +
									"(cond ((null? b) a)" +
										  "((< a (car b)) (apply max (car b) (cdr b)))" +
										   "(else (apply max a (cdr b))))" +
									"))");
		eval_defLib(parser, "(define min" +
								"(lambda (a . b)" +
									"(cond ((null? b) a)" +
										  "((> a (car b)) (apply min (car b) (cdr b)))" +
										   "(else (apply min a (cdr b))))" +
									"))");
		eval_defLib(parser, "(define length" +
								"(lambda (l)" +
									"(cond ((null? l) 0)" +
										   "(else (+ 1 (length (cdr l)))))))");
		
		eval_defLib(parser, "(define caar (lambda (pair) (car (car pair))))");
		eval_defLib(parser, "(define cadr (lambda (pair) (car (cdr pair))))");
		eval_defLib(parser, "(define cdar (lambda (pair) (cdr (car pair))))");
		eval_defLib(parser, "(define cddr (lambda (pair) (cdr (cdr pair))))");
		
		eval_defLib(parser, "(define caaar (lambda (pair) (car (car (car pair)))))");
		eval_defLib(parser, "(define caadr (lambda (pair) (car (car (cdr pair)))))");
		eval_defLib(parser, "(define cadar (lambda (pair) (car (cdr (car pair)))))");
		eval_defLib(parser, "(define caddr (lambda (pair) (car (cdr (cdr pair)))))");
		
		eval_defLib(parser, "(define cdaar (lambda (pair) (cdr (car (car pair)))))");
		eval_defLib(parser, "(define cdadr (lambda (pair) (cdr (car (cdr pair)))))");
		eval_defLib(parser, "(define cddar (lambda (pair) (cdr (cdr (car pair)))))");
		eval_defLib(parser, "(define cdddr (lambda (pair) (cdr (cdr (cdr pair)))))");
		
		eval_defLib(parser, "(define caaaar (lambda (pair) (car (car (car (car pair))))))");
		eval_defLib(parser, "(define caaadr (lambda (pair) (car (car (car (cdr pair))))))");
		eval_defLib(parser, "(define caadar (lambda (pair) (car (car (cdr (car pair))))))");
		eval_defLib(parser, "(define caaddr (lambda (pair) (car (car (cdr (cdr pair))))))");
		
		eval_defLib(parser, "(define cadaar (lambda (pair) (car (cdr (car (car pair))))))");
		eval_defLib(parser, "(define cadadr (lambda (pair) (car (cdr (car (cdr pair))))))");
		eval_defLib(parser, "(define caddar (lambda (pair) (car (cdr (cdr (car pair))))))");
		eval_defLib(parser, "(define cadddr (lambda (pair) (car (cdr (cdr (cdr pair))))))");
		
		eval_defLib(parser, "(define cdaaar (lambda (pair) (cdr (car (car (car pair))))))");
		eval_defLib(parser, "(define cdaadr (lambda (pair) (cdr (car (car (cdr pair))))))");
		eval_defLib(parser, "(define cdadar (lambda (pair) (cdr (car (cdr (car pair))))))");
		eval_defLib(parser, "(define cdaddr (lambda (pair) (cdr (car (cdr (cdr pair))))))");
		eval_defLib(parser, "(define cddaar (lambda (pair) (cdr (cdr (car (car pair))))))");
		eval_defLib(parser, "(define cddadr (lambda (pair) (cdr (cdr (car (cdr pair))))))");
		eval_defLib(parser, "(define cdddar (lambda (pair) (cdr (cdr (cdr (car pair))))))");
		eval_defLib(parser, "(define cddddr (lambda (pair) (cdr (cdr (cdr (cdr pair))))))");
		
//		eval_defLib(parser, "(define reverse" +
//								"(lambda (ls)" +
//									"(if (null? (cdr ls))" +
//										 "ls" +
//										 "(append (reverse (cdr ls)) (list (car ls))))))");
		
		eval_defLib(parser, "(define gcd" +
								"(lambda (x y)" +
									"(if (= y 0)" +
										"x" +
										"(gcd y (modulo x y)))))");
		eval_defLib(parser, "(define lcm" +
								"(lambda (x y)" +
									"(/ (abs (* x y)) (gcd x y))))");
	}
	public SInterpreter()
	{
		setBuiltinList();
		setLibrary();
	}
}
