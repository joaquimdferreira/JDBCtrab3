package jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;

import javax.print.DocFlavor.READER;

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
		RefVei,
		DevVei,
		VeiDet,
		ProLim,
		UptTip
    }

    private static Application __instance = null;
	private HashMap<Option,DbWorker> __dbMethods;
	private Connection con = null;
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

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
		__dbMethods.put(Option.AddPes, new DbWorker() {public void doWork() { Application.this.createPerson(0, "");}});
		__dbMethods.put(Option.RefVei, new DbWorker() {public void doWork() { Application.this.refreshOldTable();}});
		__dbMethods.put(Option.DevVei, new DbWorker() {public void doWork() { Application.this.devalueByLicensePlate();}});
		__dbMethods.put(Option.VeiDet, new DbWorker() {public void doWork() { Application.this.getVehicleCalcs();}});
		__dbMethods.put(Option.ProLim, new DbWorker() {public void doWork() { Application.this.checkPropLimit();}});
		__dbMethods.put(Option.UptTip, new DbWorker() {public void doWork() { Application.this.updateTipo();}});
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
			System.out.println("Empresa de Transportes XPTO");
			System.out.println();
			System.out.println(" 1.  Sair");
			System.out.println(" 2.  Adicionar Condutor");
			System.out.println(" 3.  Adicionar Pessoa");
			System.out.println(" 4.  Atualizar Veiculos");
			System.out.println(" 5.  Colocar veiculo fora de serviço");
			System.out.println(" 6.  Obter detalhes de veiculo");
			System.out.println(" 7.  Verificar o limite de carro para proprietarios");
			System.out.println(" 8.  Alterar tipo");
			System.out.println();
			System.out.print(" >");
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

		System.out.println("Insira o Id, o nr de carta de conducao e a data de nascimento da seguinte forma:");
		System.out.println("id, ncconducao, AAAA-MM-DD");
		
		try{
			String info = reader.readLine();
			String[] data = info.split("[,;.]+");
			
			id = Integer.parseInt(data[0].trim());
			ncconducao = data[1].trim();
			checkCConducao(ncconducao);
			dtnascimento = Date.valueOf(data[2].trim());
			if(!doesPersonExist(id, "C"))return;
		}catch (Exception e){
			System.out.println("Erro: " + e.getMessage() + "\n");
			System.out.println("Criação de condutor abortada!\n\n");
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
			System.out.println("Criação de condutor abortada!\n\n");
		}
	}

	private void checkCConducao(String ncconducao) throws IllegalArgumentException{
		if(ncconducao.length() != 10)throw new IllegalArgumentException("Formato invalido para carta de condução");

		if(!((int) ncconducao.charAt(0) >= 97 || 
			(int) ncconducao.charAt(0) <= 122))throw new IllegalArgumentException("formato errado para matricula");
		if(!((int) ncconducao.charAt(1) >= 97 || 
		(int) ncconducao.charAt(1) <= 122))throw new IllegalArgumentException("formato errado para matricula");

		if((int) ncconducao.charAt(2) != 45)throw new IllegalArgumentException("formato errado para matricula");

		if(!((int) ncconducao.charAt(4) >= 48 || 
		(int) ncconducao.charAt(4) <= 57))throw new IllegalArgumentException("formato errado para matricula");
		if(!((int) ncconducao.charAt(5) >= 48 || 
		(int) ncconducao.charAt(5) <= 57))throw new IllegalArgumentException("formato errado para matricula");
		if(!((int) ncconducao.charAt(4) >= 48 || 
		(int) ncconducao.charAt(4) <= 57))throw new IllegalArgumentException("formato errado para matricula");
		if(!((int) ncconducao.charAt(5) >= 48 || 
		(int) ncconducao.charAt(5) <= 57))throw new IllegalArgumentException("formato errado para matricula");
		if(!((int) ncconducao.charAt(4) >= 48 || 
		(int) ncconducao.charAt(4) <= 57))throw new IllegalArgumentException("formato errado para matricula");
		if(!((int) ncconducao.charAt(5) >= 48 || 
		(int) ncconducao.charAt(5) <= 57))throw new IllegalArgumentException("formato errado para matricula");
		if(!((int) ncconducao.charAt(5) >= 48 || 
		(int) ncconducao.charAt(5) <= 57))throw new IllegalArgumentException("formato errado para matricula");
	}

	private boolean doesPersonExist(int id, String tipo) {
		String cmd = "SELECT * FROM pessoa where id = ? and atrdic = ?";
		ResultSet result;

		try (PreparedStatement pstmt = con.prepareStatement(cmd)){
			pstmt.setInt(1, id);
			pstmt.setString(2, tipo);
			result = pstmt.executeQuery();
			if(!result.next()){
				System.out.println("Não existe uma pessoa que possa conduzir com esse id deseja criar uma?(y/n)");
				if(reader.readLine().trim().charAt(0)== 'y' && createPerson(0, tipo))return true;
				else return false;
			}
			return true;
		} catch (Exception sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
			return false;
		}
	}

	private boolean createPerson(int id, String tipo) {
		String cmd = "INSERT INTO pessoa VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String noident, nif, nproprio, apelido, morada, localidade;
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

		String newTipo = tipo;
		if(tipo.isEmpty()){
			System.out.println("Insere o tipo para a nova pessoa");
			try {
				newTipo = reader.readLine().trim();
				if(!"CLP".contains(newTipo)){
					System.out.println("Tipo invalido");
					return false;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Insira para o id = "+newId+" o noident, nif, nproprio, apelido," +
		" morada, codpostal, localidade separado por virgulas");
		
		try{
			String info = reader.readLine();
			String[] data = info.split("[,;.]+");
			
			if(data.length != 7)throw new IllegalArgumentException("Campos invalidos para pessoa");

			noident = data[0].trim();
			nif = data[1].trim();
			nproprio = data[2].trim();
			apelido = data[3].trim();
			morada = data[4].trim();
			codpostal = Integer.parseInt(data[5].trim());
			localidade = data[6].trim();
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
			pstmt.setString(9, newTipo);
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

	private void devalueByLicensePlate(){
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

		String getVehicleInfo = "select id, matricula, tipo, modelo, marca, ano, proprietario, count(id) as nrviagens "+
		"from viagem right join veiculo on viagem.veiculo = veiculo.id "+
		"where matricula = ? "+
		"group by (id, matricula , tipo, modelo, marca, ano, proprietario)";
		
		System.out.println("Indique a matricula do carro no formato 11AA11 ou AA11AA");
		String matricula;
		VeiculoOldScheme vos;
		try {
			matricula = reader.readLine().trim();
			assureLicenseFormat(matricula);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		try (PreparedStatement pstmt = con.prepareStatement(getVehicleInfo)){
			pstmt.setString(1, matricula);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				vos = extractVehicleOld(rs);
				insertVehicleOld(vos);
				deleteVehicle(vos);
				System.out.println("O veiculo com o id: "+vos.id+" ficou fora de serviço");
			} else {
				System.out.println("Veiculo não encontrado para matricula "+matricula);
			}
		} catch( SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
			return;
		}
	}

	private VeiculoOldScheme extractVehicleOld(ResultSet rs) throws SQLException {
		VeiculoOldScheme vos = new VeiculoOldScheme();
		vos.id = rs.getInt(1);
		vos.matricula = rs.getString(2);
		vos.tipo = rs.getInt(3);
		vos.modelo = rs.getString(4);
		vos.marca = rs.getString(5);
		vos.ano = rs.getInt(6);
		vos.proprietario = rs.getInt(7);
		vos.nrviagens = rs.getInt(8);
		vos.distance = calculateDistanceForVeiculo(vos.id);
		return vos;
	}

	private void insertVehicleOld(VeiculoOldScheme vos) {
		String insertCmd = "INSERT INTO veiculo_old VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = con.prepareStatement(insertCmd)) {
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

	private void deleteVehicle(VeiculoOldScheme vos) {
		String deleteFromVeiculoCmd ="DELETE FROM veiculo WHERE id = ?";
		try (PreparedStatement pstmt = con.prepareStatement(deleteFromVeiculoCmd)){
			pstmt.setInt(1, vos.id);
			pstmt.executeUpdate();
		} catch( SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
		}
	}

	private void assureLicenseFormat(String matricula) throws IllegalArgumentException {
		if(matricula.length() != 6)throw new IllegalArgumentException("Matriculas teem 6 caracteres");
		char firstChar = matricula.charAt(0);
		if((int) firstChar >= 48 || (int) firstChar <= 57){
			if(!((int) matricula.charAt(1) >= 48 || 
			(int) matricula.charAt(1) <= 57))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(2) >= 65 || 
			(int) matricula.charAt(2) <= 90))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(3) >= 65 || 
			(int) matricula.charAt(3) <= 90))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(4) >= 48 || 
			(int) matricula.charAt(4) <= 57))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(5) >= 48 || 
			(int) matricula.charAt(5) <= 57))throw new IllegalArgumentException("formato errado para matricula");
			return;
		}
		if((int) firstChar >= 65 || (int) firstChar <= 90){
			if(!((int) matricula.charAt(1) >= 65 || 
			(int) matricula.charAt(1) <= 90))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(2) >= 48 || 
			(int) matricula.charAt(2) <= 57))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(3) >= 48 || 
			(int) matricula.charAt(3) <= 57))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(4) >= 65 || 
			(int) matricula.charAt(4) <= 90))throw new IllegalArgumentException("formato errado para matricula");
			if(!((int) matricula.charAt(5) >= 65 || 
			(int) matricula.charAt(5) <= 90))throw new IllegalArgumentException("formato errado para matricula");
			return;
		}
		throw new IllegalArgumentException("formato errado para matricula");		
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
				vos = extractVehicleOld(rs);
				v.add(vos);
			}
		} catch( SQLException sqlex) {
			System.out.println("Erro: " + sqlex.getMessage() + "\n");
		}
		
		//insert into veiculo_old
		for(int i = 0; i < v.size(); i++)insertVehicleOld(v.get(i));
		
		//delete old rows from veiculo
		
		for(int i = 0; i < v.size(); i++)deleteVehicle(v.get(i));

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

	private void displayAllVehicles() {
		String cmd = "SELECT * FROM veiculo";
		ResultSet rs;
		try {
			Statement statement = con.createStatement();
            rs = statement.executeQuery(cmd);
			System.out.println("ID\tmatricula\ttipo\tmodelo\tmarca\tano\tproprietario");
			while(rs.next()){
				System.out.println(
					rs.getInt(1)+"\t"+rs.getString(2)+"\t\t"+
					rs.getInt(3)+"\t"+rs.getString(4)+"\t"+
					rs.getString(5)+"\t"+rs.getInt(6)+"\t"+
					rs.getInt(7)
				);
			}
		} catch (SQLException sqlex) {
			System.out.println("Erro: "+sqlex.getMessage());
		}
	}

	private class VehicleDetails{
		int id;
		String matricula;
		int tipo;
		String modelo;
		String marca;
		int ano;
		int proprietario;
		Time horas;
		double distance;
		double custo;
	}

	private void getVehicleCalcs() {
		displayAllVehicles();
		int id;
		System.out.println("Selecione um veiculo para obter os detalhes (ID)");
		try {
			String inp = reader.readLine().trim();
			id = Integer.parseInt(inp);
			VehicleDetails vd = extractVehicleData(id);
			System.out.println("ID\tmatricula\ttipo\tmodelo\tmarca\tano\t"+
			"proprietario\thoras totais\tkilometros\tcusto total");
			System.out.println(
				vd.id+"\t"+
				vd.matricula+"\t\t"+
				vd.tipo+"\t"+
				vd.modelo+"\t"+
				vd.marca+"\t"+
				vd.ano+"\t"+
				vd.proprietario+"\t\t"+
				vd.horas+"\t"+
				String.format("%.2f", vd.distance)+"\t\t"+
				vd.custo
			);

		} catch(Exception e) {
			System.out.println("Erro: "+e.getMessage());
			return;
		}
	}

    private VehicleDetails extractVehicleData(int id) throws Exception {
		String findCmd = "select * from veiculo where id = ?";
		VehicleDetails vd = new VehicleDetails();
		
		PreparedStatement pstmt = con.prepareStatement(findCmd);
		pstmt.setInt(1, id);
		ResultSet rs = pstmt.executeQuery();
		if(!rs.next())throw new Exception("Veiculo não encontrado");

		vd.id = rs.getInt(1);
		vd.matricula = rs.getString(2);
		vd.tipo = rs.getInt(3);
		vd.modelo = rs.getString(4);
		vd.marca = rs.getString(5);
		vd.ano = rs.getInt(6);
		vd.proprietario = rs.getInt(7);
		vd.horas = calculateHours(id);
		vd.distance = calculateDistanceForVeiculo(id);
		vd.custo = calculateCusto(id);
		return vd;
	}

	private double calculateCusto(int id) throws Exception {
		String getCmd = "select sum(valfinal) from viagem where veiculo = ?";
		PreparedStatement pstmt = con.prepareStatement(getCmd);
		pstmt.setInt(1, id);
		ResultSet rs = pstmt.executeQuery();
		if(!rs.next())throw new Exception("Não foram encontradas viagens terminadas para o veiculo "+id);
		return rs.getDouble(1);
	}

	private Time calculateHours(int id) throws Exception {
		long  milisBetween = 0L;
		String getFinishedHours = "select hinicio, hfim from viagem where veiculo = ? and hfim is not null";
		Time t1;
		Time t2;

		PreparedStatement pstmt = con.prepareStatement(getFinishedHours);
		pstmt.setInt(1, id);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			t1 = rs.getTime(1);
			t2 = rs.getTime(2);
			milisBetween += (t2.getTime()-t1.getTime());
		}
		return new Time(milisBetween);
	}

	private void checkPropLimit(){
		String obtainCount = "select idpessoa, count(idpessoa) "+
		"from veiculo left join proprietario on veiculo.proprietario = proprietario.idpessoa "+
		"group by (idpessoa)";

		int id, count;
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(obtainCount);
			while(rs.next()){
				id = rs.getInt(1);
				count = rs.getInt(2);
				if(count > 20)deletePropVei(id, count-20);
				System.out.println("Limite verificado");
			}
		} catch (SQLException sqlex) {
			System.out.println("Erro: "+sqlex.getMessage());
		}
	}

	private void deletePropVei(int id, int n) throws SQLException {
		String getVehicleList = "select id from veiculo where proprietario = ?";
		String deleteVehicle = "DELETE FROM veiculo WHERE id = ?";
		PreparedStatement pstmt = con.prepareStatement(getVehicleList);
		pstmt.setInt(1, id);
		ResultSet rs = pstmt.executeQuery();
		PreparedStatement pstmt2 = con.prepareStatement(deleteVehicle);
		for(int i = 0; i < n; i++){
			if(rs.next()) {
				pstmt2.setInt(1, rs.getInt(1));
				pstmt2.executeUpdate();
			}
		}

	}

	private void updateTipo() {
		System.out.println("Indique o tipo a alterar");
		
		try {
			int tipo = Integer.parseInt(reader.readLine().trim());
			System.out.println("Indique a nova designação");
			String newDes = reader.readLine().trim();
			System.out.println("Que outras alterações vai fazer ao tipo?");
			System.out.println(" 1.  Nenhuma");
			System.out.println(" 2.  Nr de lugares");
			System.out.println(" 3.  Multipliador");
			System.out.println(" 4.  Ambas");

			String input = reader.readLine();
			String cmd;
			int res = Integer.parseInt(input);
			System.out.println(res);
			switch(res) {
				case 1:
					cmd = "update tipoveiculo set designacao = ? where tipo = ?";
					PreparedStatement pstmt = con.prepareStatement(cmd);
					pstmt.setString(1, newDes);
					pstmt.setInt(2, tipo);
					pstmt.executeUpdate();
					break;
				case 2:
					cmd = "update tipoveiculo set designacao = ?, nlugares = ? where tipo = ?";
					PreparedStatement pstmt2 = con.prepareStatement(cmd);
					pstmt2.setString(1, newDes);
					System.out.println("Insira nr de lugares");
					pstmt2.setInt(2, Integer.parseInt(reader.readLine()));
					pstmt2.setInt(3, tipo);
					pstmt2.executeUpdate();
					break;
				case 3:
					cmd = "update tipoveiculo set designacao = ?, multiplicador = ? where tipo = ?";
					PreparedStatement pstmt3 = con.prepareStatement(cmd);
					pstmt3.setString(1, newDes);
					System.out.println("Insira multiplicador");
					pstmt3.setInt(2, Integer.parseInt(reader.readLine()));
					pstmt3.setInt(3, tipo);
					pstmt3.executeUpdate();
					break;
				case 4:
					cmd = "update tipoveiculo set designacao = ?, nlugares = ?, multiplicador = ? where tipo = ?";
					PreparedStatement pstmt4 = con.prepareStatement(cmd);
					pstmt4.setString(1, newDes);
					System.out.println("Insira nr de lugares");
					pstmt4.setInt(2, Integer.parseInt(reader.readLine()));
					System.out.println("Insira multiplicador");
					pstmt4.setInt(3, Integer.parseInt(reader.readLine()));
					pstmt4.setInt(4, tipo);
					pstmt4.executeUpdate();
					break;
				default:
					System.out.println("Erro na sua parte");
					return;
			}
		} catch(Exception e) {
			System.out.println("Erro: "+e.getMessage());
		}
		System.out.println("Tipo alterado");
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

