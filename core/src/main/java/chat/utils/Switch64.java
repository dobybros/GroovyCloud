package chat.utils;
public class Switch64 {
	private long switch64;
	
	private static final int MAX = 62;
	
	public Switch64(long switch64) {
		this.switch64 = switch64;
	}
	
	public Switch64() {
		this.switch64 = 0;
	}
	
	public void setSwitch(int position, int switchOn) {
		setSwitch(position, switchOn != 1? false : true);
	}
	
	public void setSwitch(int position, boolean switchOn) {
		if(position < 0 || position > MAX) {
			throw new IllegalArgumentException("Illegal argument, range is 0 ~ 62 as using signed long");
		}
		long temp = 1L << (position);
//		System.out.println("temp = " + Long.toBinaryString(temp));
		if(switchOn) {
			switch64 |= temp;
//			System.out.println("base = " + Long.toBinaryString(switch64));
		} else {
			temp = ~temp;
//			System.out.println("tomp = " + Long.toBinaryString(temp));
			switch64 &= temp;
//			System.out.println("base = " + Long.toBinaryString(switch64));
		}
	}
	
	public boolean isSwitchOn(int position) {
		if(position < 0 || position > MAX) {
			throw new IllegalArgumentException("Illegal argument, range is 0 ~ 62 as using signed long");
		}
//		System.out.println("pos = " + position);
		long temp = 1L << (position);
//		System.out.println("temp = " + Long.toBinaryString(temp));
//		System.out.println("base = " + Long.toBinaryString(switch64));
		return (switch64 & temp) != 0;
	}
	
	public long getValue() {
		return switch64;
	}
	public void setValue(long switch64) {
		this.switch64 = switch64;
	}
	public long getSwitch64() {
		return switch64;
	}

	public void setSwitch64(long switch64) {
		this.switch64 = switch64;
	}

	public static void main(String[] args) {
		Switch64 switches = new Switch64(Long.MAX_VALUE);
		System.out.println("test " + switches.isSwitchOn(5));
		switches.setSwitch(5, false);
		System.out.println("test " + switches.isSwitchOn(5));
		
		String str = Long.toBinaryString(Long.MAX_VALUE);
		System.out.println("str1 = " + str);
		
		Switch64 data = new Switch64(Long.parseLong("110111111111111111111111111111111111111111111111111111111111100", 2));
		data.setSwitch(30, false);
		data.setSwitch(31, false);
		data.setSwitch(0, true);
		data.setSwitch(62, true);
		System.out.println("rest = " + Long.toBinaryString(data.getValue()));
		
//		System.out.println("result = " + new Switch64(Long.parseLong("111111111111111111111111111111111111111111111111111111111111110", 2)).isSwitchOn(10));
		
//		System.out.println(Long.toBinaryString(Long.MAX_VALUE).length());
	}
}
