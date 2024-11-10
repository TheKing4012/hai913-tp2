public class B {

    public void methodB1() {
        System.out.println("Executing methodB1 in class test.B");
        A a = new A();
        a.methodA2(); // Appel vers une méthode de la classe test.A
    }

    public void methodB2() {
        System.out.println("Executing methodB2 in class test.B");
    }

    public void methodB3() {
        System.out.println("Executing methodB3 in class test.B");
    }
}
