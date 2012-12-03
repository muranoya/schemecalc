package schemecalc;

public enum TokenKind
{
	Others,
    Lparen, Rparen,
    Plus, Minus, Mul, Mod, Divi,
    Period, Apostrophe, DblQ, Sharp,
    IntNum, DoubleNum, True, False,
    Ident, String, Char, NulKind, Letter, Digit, EofTkn
}
