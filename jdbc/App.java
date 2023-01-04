package jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ArrayList;

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
        Exit,
		AddCon,
		AddPes,
		RefVei
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
        __dbMethods.put(Option.AddCon, new DbWorker() {public void doWork() { Application.this.insertCondutor();}});
		__dbMethods.put(Option.AddPes, new DbWorker() {public void doWork() { Application.this.createPerson(0);}});
		__dbMethods.put(Option.RefVei, new DbWorker() {public void doWork() { Application.this.refreshOldTable();;}});
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
        
	
		try
		{
			System.out.println("Empresa de Transportes X");
			System.out.println();
			System.out.println(" 1.  Sair");
			System.out.println(" 2.  Adicionar Condutor");
			System.out.println(" 3.  Adicionar Pessoa");
			System.out.println(" 4.  Atualizar Veiculos");
			System.out.println();
			System.out.print(" >");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String result = reader.readLine();
			option = Option.values()[Integer.parseInt(result)];			
		}
		catch(IOException ex) { System.out.print("IOException"); }
		
		return option;

	}

	private void insertCondutor() {
		String cmd = "INSERT INTO condutor VALUES(?, ?, ?)";
		int id;
		String ncconducao;
		Date dtnascimento;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Insira o Id, o nr de carta de conducao e a data de nascimento da seguinte forma:");
		System.out.println("id, ncconducao, AAAA-MM-DD");
		
		try{
			String info = reader.readLine();
			String[] data = info.split("[,;.]+");
			
			id = Integer.parseInt(data[0].trim());
			ncconducao = data[1].trim();
			dtnascimento = Date.valueOf(data[2].trim());
			if(!doesPersonExist(id))return;
		}catch (Exception e){
			System.out.println("Erro: " + e.getMessage() + "\n");
			return;
		}

		try (PreparedStatement pstmt = con.prepareStatement(cmd)){
			pstmt.setInt(1, id);
			pstmt.setString(2, ncconducao);
			pstmt.setDate(3, dtnascimento);
			pstmt.executeUpdate();

			System.out.println("Condutor adicionado com sucesso!\n\n");
		} catch (SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
		}
	}

	private boolean doesPersonExist(int id) {
		String cmd = "SELECT * FROM pessoa where id = ?";
		ResultSet result;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try (PreparedStatement pstmt = con.prepareStatement(cmd)){
			pstmt.setInt(1, id);
			result = pstmt.executeQuery();
			if(!result.next()){
				System.out.println("Não existe uma pessoa com esse id deseja criar uma?(y/n)");
				String answer = "";
				try {
					answer = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(answer.charAt(0)== 'y' && createPerson(id))return true;
				else return false;
			}
			else return true;
		} catch (SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
			return false;
		}
	}

	private boolean createPerson(int id) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String cmd = "INSERT INTO pessoa VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String noident, nif, nproprio, apelido, morada, localidade, atrdisc;
		int codpostal;

		int newId = id;
		if(id == 0){
			System.out.println("Insere o id para a nova pessoa");
			try {
				newId = Integer.parseInt(reader.readLine().trim());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Insira para o id = "+newId+" o noident, nif, nproprio, apelido," +
		"\n morada, codpostal, localidade e tipo de pessoa(CL, P, CD) separado por virgulas");
		
		try{
			String info = reader.readLine();
			String[] data = info.split("[,;.]+");
			
			if(data.length != 8)throw new IllegalArgumentException("Campos invalidos para pessoa");

			noident = data[0].trim();
			nif = data[1].trim();
			nproprio = data[2].trim();
			apelido = data[3].trim();
			morada = data[4].trim();
			codpostal = Integer.parseInt(data[5].trim());
			localidade = data[6].trim();
			atrdisc = data[7].trim();
			if(!"CLPCD".contains(atrdisc))throw new IllegalArgumentException("Campo tipo de pessoa invalido para pessoa");
		}catch (Exception e){
			System.out.println("Erro: " + e.getMessage() + "\n");
			return false;
		}

		try (PreparedStatement pstmt = con.prepareStatement(cmd)){
			pstmt.setInt(1, newId);
			pstmt.setString(2, noident);
			pstmt.setString(3, nif);
			pstmt.setString(4, nproprio);
			pstmt.setString(5, apelido);
			pstmt.setString(6, morada);
			pstmt.setInt(7, codpostal);
			pstmt.setString(8, localidade);
			pstmt.setString(9, atrdisc);
			pstmt.executeUpdate();

			System.out.println("Pessoa adicionada com sucesso!\n\n");
			return true;
		} catch (SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
			return false;
		}
	}

	private void refreshOldTable() {
		String checkTableExist = "SELECT * FROM VEICULO_OLD";
		Boolean exists;
		try {
			Statement statement = con.createStatement();
            statement.executeQuery(checkTableExist);
			exists = true;
		} catch(SQLException sqlex) {
			exists = false;
		}
		if(!exists)createOldTable();

		updateTablesData();
	}

	private class VeiculoOldScheme{
		int id;
		String matricula;
		int tipo;
		String modelo;
		String marca;
		int ano;
		int proprietario;
		int nrviagens;
		double distance;
	}

	private void updateTablesData() {
		String getVehicleInfo = "select id, matricula, tipo, modelo, marca, ano, proprietario, count(id) as nrviagens "+
		"from viagem left join veiculo on viagem.veiculo = veiculo.id "+
		"where ano <= ? "+
		"group by (id, matricula , tipo, modelo, marca, ano, proprietario) "+
		"order by id;";
		int year = Calendar.getInstance().get(Calendar.YEAR) - 5;
		ArrayList<VeiculoOldScheme> v = new ArrayList<VeiculoOldScheme>();
		VeiculoOldScheme vos;
		//store rows in array list of class
		try (PreparedStatement pstmt = con.prepareStatement(getVehicleInfo)){
			pstmt.setInt(1, year);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				vos = new VeiculoOldScheme();
				vos.id = rs.getInt(1);
				vos.matricula = rs.getString(2);
				vos.tipo = rs.getInt(3);
				vos.modelo = rs.getString(4);
				vos.marca = rs.getString(5);
				vos.ano = rs.getInt(6);
				vos.proprietario = rs.getInt(7);
				vos.nrviagens = rs.getInt(8);
				vos.distance = calculateDistanceForVeiculo(vos.id);
				v.add(vos);
			}
		} catch( SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
		}
		
		//insert into veiculo_old
		String insertCmd = "INSERT INTO veiculo_old VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		for(int i = 0; i < v.size(); i++) {
			try (PreparedStatement pstmt = con.prepareStatement(insertCmd)){
				vos = v.get(i);
				pstmt.setInt(1, vos.id);
				pstmt.setString(2, vos.matricula);
				pstmt.setInt(3, vos.tipo);
				pstmt.setString(4, vos.modelo);
				pstmt.setString(5, vos.marca);
				pstmt.setInt(6, vos.ano);
				pstmt.setInt(7, vos.proprietario);
				pstmt.setInt(8, vos.nrviagens);
				pstmt.setDouble(9, vos.distance);
				pstmt.executeUpdate();
				
			} catch( SQLException sqlex) {
				System.out.println("Erro: " + sqlex.getMessage() + "\n");
			}
		}
		
		//delete old rows from veiculo
		String deleteFromVeiculoCmd ="DELETE FROM veiculo WHERE id = ?";
		for(int i = 0; i < v.size(); i++) {
			try (PreparedStatement pstmt = con.prepareStatement(deleteFromVeiculoCmd)){
				vos = v.get(i);
				pstmt.setInt(1, vos.id);
				pstmt.executeUpdate();
				
			} catch( SQLException sqlex) {
				System.out.println("Erro: " + sqlex.getMessage() + "\n");
			}
		}

		System.out.println("Tabelas atualizadas");
	}

	private double calculateDistanceForVeiculo(int id) {
		String cmd = "select cast(latinicio as numeric), cast(longinicio as numeric),"+
		" cast(latfim as numeric), cast(longfim as numeric) "+
		"from viagem where veiculo = ?";

		double totalDistance = 0.0;
		try (PreparedStatement pstmt = con.prepareStatement(cmd)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				totalDistance += distance(
					rs.getDouble(1),
					rs.getDouble(2),
					rs.getDouble(3),
					rs.getDouble(4),
					"K"
				);
			}
		} catch( SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
		}
		return totalDistance;
	}

	private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		if ((lat1 == lat2) && (lon1 == lon2)) {
			return 0;
		}
		else {
			double theta = lon1 - lon2;
			double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + 
			Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			if (unit.equals("K")) {
				dist = dist * 1.609344;
			} else if (unit.equals("N")) {
				dist = dist * 0.8684;
			}
			return (dist);
		}
	}

	private void createOldTable(){
		System.out.println("Tabela VEICULO_OLD não encontrada, irá ser criada...");
		String createCmd = "CREATE TABLE VEICULO_OLD (" +
		"id integer not null, matricula varchar(10) not null check(matricula like '__%%__' or matricula like '%%__%%'), " +
		"tipo integer not null, modelo varchar(10) not null, marca varchar(10) not null, " +
		"ano integer not null, proprietario integer not null, nrviagens integer not null, kms decimal(10,2) not null);";

		try {
			Statement statement = con.createStatement();
            statement.executeUpdate(createCmd);
			System.out.println("Tabela criada com sucesso");
		} catch(SQLException sqlex) {
			System.out.println("Erro: "+sqlex.getMessage());
		}
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
				System.in.read();
			}
			catch(NullPointerException ex) {
				System.out.print("");
			}
		}while(userInput!=Option.Exit);
		Logout();
	}
}

