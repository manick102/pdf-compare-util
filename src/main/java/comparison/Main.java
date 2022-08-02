package comparison;

import org.testng.TestNG;

public class Main {
    public static void main(String[] args) {
        TestNG testng = new TestNG();
        try {
            Class[] classes = {Class.forName("comparison.Runner")};
            testng.setTestClasses(classes);
            testng.run();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
