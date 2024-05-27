import test.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LoginTest loginTest = new LoginTest();
        loginTest.performLoginFromCSV("data/login.csv");

        RegisterTest registerTest = new RegisterTest();
        registerTest.performRegisterFromCSV("data/register.csv");

        AddStudentTest addStudentTest = new AddStudentTest();
        addStudentTest.performAddStudentFromCSV("data/add_student.csv");

        UpdateStudentTest updateStudentTest = new UpdateStudentTest();
        updateStudentTest.performUpdateStudentFromCSV("data/update_student.csv");

//        DeleteStudentTest deleteStudentTest = new DeleteStudentTest();
//        deleteStudentTest.deleteStudentTest("123123", "12345678", "31");
    }
}
