package dk.brics.paddle;
public class Test1 {
	
	static int DUMMY=3;
	
	public void go() {
	
		DUMMY++;
		
		Container c1 = new Container();
		Item i1 = new Item();
		c1.setItem(i1);
		
		Container c2 = new Container();
		Item i2 = new Item();
		c2.setItem(i2);	
		
		Container c3 = c2;
	}
	
	public static void main(String args[]) {
		DUMMY--;
		new Test1().go();
	}

}
