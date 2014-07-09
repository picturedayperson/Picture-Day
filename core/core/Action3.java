package core;

public interface Action3<Operand1, Operand2, Operand3>
{
	void perform(Operand1 operand1, Operand2 operand2, Operand3 operand3);
}