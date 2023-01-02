package jdbc;

import java.sql.*;
//import java.time.LocalDate;
//import java.util.Calendar;
import java.util.Scanner;
import java.util.HashMap;

public class App {
    static String jdbcURL = "jdbc:postgresql://10.62.73.73:5432/mp27";
    static String username = "mp27";
    static String password = "grupo27";

    public static void main(String[] args) throws Exception {
		Application.getInstance().Run(jdbcURL, username, password);
	}
}

interface DbWorker {
	void doWork();
}

class Application {
    private enum Option {
        Unknown,
        Exit
    }

    private static Application __instance = null;
	private HashMap<Option,DbWorker> __dbMethods;
	private Connection con = null;

    public static Application getInstance() {
		if(__instance == null) 
		{
			__instance = new Application();
		}
		return __instance;
	}

    private Application() {
		__dbMethods = new HashMap<Option,DbWorker>();
        //put ....
	}

    private void Login(String jdbcURL, String username, String password) throws SQLException {
		con = DriverManager.getConnection(jdbcURL, username, password);
	}

    private void Logout() throws SQLException {
		if (con != null) {
			con.close();
		}
		con = null;
	}

    private final static void clearConsole() throws Exception {
	    for (int y = 0; y < 25; y++) //console is 80 columns and 25 lines
	    System.out.println("\n");
	}

    private Option DisplayMenu() {
		Option option=Option.Unknown;
        Scanner s = new Scanner(System.in);
		try
		{
			System.out.println("Gestão do Campo de Férias");
			System.out.println();
			System.out.println("	1.  Sair");
			System.out.println();
			System.out.print("	>");
			int result = s.nextInt();
			option = Option.values()[result];			
		}
		catch(RuntimeException ex) { System.out.print(""); }
        finally { s.close(); }
		
		return option;

	}

    public void Run(String jdbcURL, String username, String password) throws Exception {
		Login(jdbcURL, username, password);
		Option userInput = Option.Unknown;
		do {
			clearConsole();
			userInput = DisplayMenu();
			clearConsole();		  	
			try {
				__dbMethods.get(userInput).doWork();	// Executa o método correspondente à opção escolhida
				System.out.println("Pressione a tecla \"Enter\" para continuar... ");
				System.in.read();
			}
			catch(NullPointerException ex) {
				System.out.print("");
			}
		}while(userInput!=Option.Exit);
		Logout();
	}
}

