package com.example.recorder5;


/*************************************************************************
 *  Compilation:  javac Matrix.java
 *  Execution:    java Matrix
 *
 *  A bare-bones collection of static methods for manipulating
 *  matrices.
 *
 *************************************************************************/

public class Matrix {

    // return a random m-by-n matrix with values between 0 and 1
    public static double[][] random(int m, int n) {
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = Math.random();
        return C;
    }

    // return n-by-n identity matrix I
    public static double[][] identity(int n) {
        double[][] I = new double[n][n];
        for (int i = 0; i < n; i++)
            I[i][i] = 1;
        return I;
    }

    // return x^T y
    public static double dot(double[] x, double[] y) {
        if (x.length != y.length) throw new RuntimeException("Illegal vector dimensions.");
        double sum = 0.0;
        for (int i = 0; i < x.length; i++)
            sum += x[i] * y[i];
        return sum;
    }

    // return C = A^T
    public static double[][] transpose(double[][] A) {
        int m = A.length;
        int n = A[0].length;
        double[][] C = new double[n][m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[j][i] = A[i][j];
        return C;
    }

    // return C = A + B
    public static double[][] add(double[][] A, double[][] B) {
        int m = A.length;
        int n = A[0].length;
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] + B[i][j];
        return C;
    }

    // return C = A - B
    public static double[][] subtract(double[][] A, double[][] B) {
        int m = A.length;
        int n = A[0].length;
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] - B[i][j];
        return C;
    }

    // return C = A * B
    public static double[][] multiply(double[][] A, double[][] B) {
        int mA = A.length;
        int nA = A[0].length;
        int mB = B.length;
        int nB = A[0].length;
        if (nA != mB) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] C = new double[mA][nB];
        for (int i = 0; i < mA; i++)
            for (int j = 0; j < nB; j++)
                for (int k = 0; k < nA; k++)
                    C[i][j] += (A[i][k] * B[k][j]);
        return C;
    }

    // matrix-vector multiplication (y = A * x)
    public static double[] multiply(double[][] A, double[] x) {
        int m = A.length;
        int n = A[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                y[i] += (A[i][j] * x[j]);
        return y;
    }

    // matrix-vector multiplication (y = A * x), assuming square matrix and 
    public static void flatmultiply( float[] y, float[] A, float[] x) {
    	
        int m = A.length;
        int n=(int)Math.sqrt(m);
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        //double[] y = new double[n];
        for (int i = 0; i < n; i++){
        	y[i]=0;
            for (int j = 0; j < n; j++)
                y[i] += (A[i*n+j] * x[j]);
        }
    //    return y;
    }

    
    // vector-matrix multiplication (y = x^T A)
    public static double[] multiply(double[] x, double[][] A) {
        int m = A.length;
        int n = A[0].length;
        if (x.length != m) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[n];
        for (int j = 0; j < n; j++)
            for (int i = 0; i < m; i++)
                y[j] += (A[i][j] * x[i]);
        return y;
    }
    
    public static void arrayprint(double[][] d) {
    	
        System.out.print(d[0][0]+" "+d[0][1]+" "+d[0][2]+" "+d[1][0]+" "+d[1][1]+" "+d[1][2]+" "+d[2][0]+" "+d[2][1]+" "+d[2][2]);
    
    }

   public static void arrayprint(float[] d) {
	      int m = d.length;
	        int n=(int)Math.sqrt(m);
	        for (int i=0;i<n;i++){
	        	for (int j=0;j<n;j++){
	        		System.out.print(d[i*n+j]+" ");
	        	}
	        	System.out.println();
	        }    
    }

   
   public static void vectorprint(float[] d) {
	      int m = d.length;

	        for (int i=0;i<m;i++){
	 
	        		System.out.print(d[i]+" ");
	        	}
	        	System.out.println();
	        }    
   public static void vectorprint(double[] d) {
	      int m = d.length;

	        for (int i=0;i<m;i++){
	 
	        		System.out.print(d[i]+" ");
	        	}
	        	System.out.println();
	        }    

   
    // test client
//    public static void main(String[] args) {
//        System.out.println("D");
//        System.out.println("--------------------");
//        double[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3} };
//        arrayprint(d);
//        System.out.println();
//
//        System.out.println("I");
//        System.out.println("--------------------");
//        double[][] c = Matrix.identity(5);
//        arrayprint(c);
//        System.out.println();
//
//        System.out.println("A");
//        System.out.println("--------------------");
//        double[][] a = Matrix.random(5, 5);
//        arrayprint(a);
//        System.out.println();
//
//        System.out.println("A^T");
//        System.out.println("--------------------");
//        double[][] b = Matrix.transpose(a);
//        arrayprint(b);
//        System.out.println();
//
//        System.out.println("A + A^T");
//        System.out.println("--------------------");
//        double[][] e = Matrix.add(a, b);
//        arrayprint(e);
//        System.out.println();
//
//        System.out.println("A * A^T");
//        System.out.println("--------------------");
//        double[][] f = Matrix.multiply(a, b);
//        arrayprint(f);
//        System.out.println();
//    }
}
