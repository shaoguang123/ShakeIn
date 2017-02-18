package com.shakein;


public class Tuple {
	public float x;
	public float y;
	public float z;

	public Tuple(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Tuple(Tuple t){
		this.x=t.x;
		this.y=t.y;
		this.z=t.z;
	}
	public static float mod(Tuple A) {
		return (float) Math.sqrt(A.x * A.x + A.y * A.y + A.z * A.z);
	}

}

