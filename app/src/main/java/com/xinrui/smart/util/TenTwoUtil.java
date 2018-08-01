package com.xinrui.smart.util;

public class TenTwoUtil {

	public static int[] changeToTwo (int x){

		int[] result = new int[8];
		result[7]=x/128;
		result[6]=(x%128)/64;
		result[5]=(x%64)/32;
		result[4]=(x%32)/16;
		result[3]=(x%16)/8;
		result[2]=(x%8)/4;
		result[1]=(x%4)/2;
		result[0]=(x%2)/1;
		return result;
	}
	
	public static int changeToTen(int[] x){
		int result =0;
		if(x[0]==1){
			result += 128;
		}
		if(x[1]==1){
			result += 64;
		}
		if(x[2]==1){
			result += 32;
		}
		if(x[3]==1){
			result += 16;
		}
		if(x[4]==1){
			result +=8;
		}
		if(x[5]==1){
			result += 4;
		}
		if(x[6]==1){
			result += 2;
		}
		if(x[7]==1){
			result += 1;
		}
		return result;
	}
	
}
