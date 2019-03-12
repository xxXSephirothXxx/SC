import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cliente {
	String username;
	String password;
	HashMap<String,File> listaFicheiros;
	HashMap<String,ArrayList<String>> listaSeguidores;
	HashMap<String,String> listaMensagens;
	
	public Cliente(String username, String password) {
		this.username=username;
		this.password=password;
		listaFicheiros=new HashMap<>();
		listaSeguidores = new HashMap<>();
		listaMensagens=new HashMap<>();
	}
	
	//Info Pessoal do Cliente
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	//Info sobre os ficheiros do cliente
	public void addFicheiro(String nome,File novo){
		listaFicheiros.put(nome,novo);
	}
	
	public void removeFicheiro(String nome) {
		listaFicheiros.remove(nome);
	}
	
	public void getFicheiro(String nomeFicheiro) {
		listaFicheiros.get(nomeFicheiro);
	}
	
	public int numFicheiros(){
		return listaFicheiros.size();
	}
	
	public ArrayList<String> getListaFicheiros() {
		ArrayList<String> lista=new ArrayList<>();
		for (String nome : listaFicheiros.keySet()) {
			lista.add(nome);
		}
		return lista;
	}
	

	public ArrayList<String> getSeguidores() {
		ArrayList<String> seguidores= new ArrayList<>();
		for (String seguidor : listaSeguidores.keySet()) {
			seguidores.add(seguidor);
		}
		return seguidores;
	}

	
	public void addSeguidor(String nome) {
		listaSeguidores.put(nome, new ArrayList<String>());
	}
	
	public void removeSeguidor(String nome) {
		listaSeguidores.remove(nome);
	}
	
	//Info sobre as mensagens
	public ArrayList<String> getListaMensagens() {
		ArrayList<String> lista=new ArrayList<>();
		for (String nome : listaMensagens.keySet()) {
			lista.add(nome);
		}
		return lista;
	}

	public boolean isTrusted(String trusted) {
		if(listaSeguidores.containsKey(trusted)==false) {
			return true;
		}else {
			return false;
		}
	}
	

	public boolean isUntrusted(String untrusted) {
		if(listaSeguidores.containsKey(untrusted)==false) {
			return false;
		}else {
			return true;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cliente other = (Cliente) obj;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
