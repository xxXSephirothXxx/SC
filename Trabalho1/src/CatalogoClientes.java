import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CatalogoClientes {

	HashMap<String,Cliente> listaClientes;
	int numClientes;


	public CatalogoClientes() throws IOException{
		listaClientes=new HashMap<String,Cliente>();
		lerClientes();		
	}

	public void lerClientes() throws IOException{
		File file = new File("./clientes.txt"); 
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while ((st = br.readLine()) != null){
			String[] palavras= st.split(":");
			String[] user=palavras[0].split("<");
			String[] user1=user[1].split(">");
			String[] pw=palavras[1].split("<");
			String[] pw1=pw[1].split(">");
			Cliente novo = new Cliente(user1[0],pw1[0]);
			listaClientes.put(user1[0], novo);
			String nomeDaPasta="./"+user1[0];
			new File(nomeDaPasta).mkdir();
			numClientes++;
		}
	}
	//Falta o escrever no ficheiro

	public void addCliente(Cliente user)  {
		File file = new File("./clientes.txt"); 
		FileWriter fw;
		try {
			fw = new FileWriter(file,true);
			listaClientes.put(user.getUsername(), user);
			fw.append("<"+user.getUsername()+">:<"+user.getPassword()+">");
			fw.write(System.getProperty( "line.separator" ));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	 * 
	 * @param nome
	 * @param pw
	 * @return
	 */
	public int verificar(String nome, String pw) {
		if(listaClientes.containsKey(nome)) {
			if(listaClientes.get(nome).getPassword().equals(pw)){
				return 2;//existe e a pw esta correcta
			}
			return 1;//existe a pw esta incorrecta
		}
		return 0;//nao existe

	}

	public Cliente getUser(String user) {
		return listaClientes.get(user);		
	}

	public StringBuilder getusers() {
		StringBuilder lista= new StringBuilder();
		for(String nome:listaClientes.keySet()){
			lista.append(nome+"\n");
		}
		return lista;
	}
}
