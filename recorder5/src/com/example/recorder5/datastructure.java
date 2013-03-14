package com.example.recorder5;



public class datastructure  {
float vals[];
String s;

	public datastructure(int N){
	vals=new float[N];  
	s="";
	}
	public void set_values(float values[])
	{
		int i=0;
		for(i=0;i<values.length;i++)
		{	
			vals[i]=values[i];
		}

	}
	
	public void set_strn(String st)
	{
		s=st;
	}
	public void fillchar(String st)
	{
		s=st;
	}

	public void printer()
	{
		int i=0;
		System.out.print(s+" ");
		for (i=0;i<vals.length;i++){
			System.out.print("v"+i+":"+Math.round(vals[i]*100.0)/100.0+" ");
		}
		System.out.println();
	}
	public void mycopy(datastructure from)
	{
		if (vals.length==from.vals.length){
		for(int i=0;i<vals.length;i++)
		{	
			vals[i]=from.vals[i];
		}
		s=from.s;
		}
	}
	

}