public class A {

    public void methodA1() {
        System.out.println("Executing methodA1 in class test.A");
        B b = new B();
        b.methodB1(); // Appel vers une méthode de la classe test.B
    }

    public void methodA2() {
        System.out.println("Executing methodA2 in class test.A");
        B b = new B();
        b.methodB2(); // Appel vers une autre méthode de la classe test.B
    }
}
