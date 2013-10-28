package backend;

import intermediate.IntermediateCode;

public class CodeGenerator 
{
	IntermediateCode icode;
	
	public CodeGenerator(IntermediateCode icode)
	{
		this.icode = icode;
	}
	
	public void traverseandprint()
	{
		System.out.println("\nRebuilding tree:");
		icode.printTrees();
	}
}
