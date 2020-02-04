import java.util.Scanner;

public class Silvia {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Scanner sc = new Scanner(System.in);
		System.out.println("¿Cual es tu nombre?");
		String str = sc.nextLine();
		System.out.println("Hola " + str);
		sc.close();
	}

}
