import java.io.BufferedInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class MsgFile {

	static String hostname;
	static String name;
	static String pw;
	static int socketCliente;
	static ObjectInputStream in;
	static ObjectOutputStream out;
	static Scanner sc;

	public static void main(String[] args) {
		sc = new Scanner(System.in);
		MsgFile client = new MsgFile();

		System.out.println("Cliente: main");

		socketCliente=selSocket(args[0]);
		hostname=selHostName(args[0]);
		String name=selName(args[1]);
		String pw=selPw(args[2]);

		client.startClient(name,pw);
	}

	private static String selName(String input) {
		String[] servPw= input.split("<");
		String[] servPw2= servPw[1].split(">");
		return servPw2[0];
	}

	private static String selPw(String input) {
		String[] servPw= input.split("\\[");
		String[] servPw2= servPw[1].split("\\]");
		return servPw2[0];
	}

	private static int selSocket(String input){
		String[] servAdress= input.split("<");
		String[] servAdress2= servAdress[1].split(">");
		String[] port= servAdress2[0].split(":");
		return Integer.parseInt(port[1]);
	}

	private static String selHostName(String input){
		String[] servAdress= input.split("<");
		String[] servAdress2= servAdress[1].split(">");
		String[] hostname= servAdress2[0].split(":");
		return hostname[0];
	}

	public void startClient(String name, String pw) {
		Socket echoSocket = null;

		try {
			echoSocket = new Socket(hostname, socketCliente);
			in = new ObjectInputStream(echoSocket.getInputStream());
			out = new ObjectOutputStream(echoSocket.getOutputStream());
			Object user = (Object) name;
			Object passwd = (Object) pw;
			out.writeObject(user);
			out.writeObject(passwd);
			Boolean fromServer = (Boolean) in.readObject();
			//password incorrecta

			while(!fromServer) {
				System.out.println("Password incorrecta");
				System.out.println("Password:");
				pw = sc.nextLine();
				passwd = (Object) pw;
				out.writeObject(passwd);
				fromServer = (Boolean) in.readObject();
			}

			if(fromServer) {
				menu();
			}

			sc.close();
			out.close();
			in.close();
			echoSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void menu() throws IOException, ClassNotFoundException {

		int num=1;
		while(num>0) {
			System.out.println("Menu do Cliente:");
			System.out.println("store");
			System.out.println("list");
			System.out.println("remove");
			System.out.println("users");
			System.out.println("trusted");
			System.out.println("untrusted");
			System.out.println("download");
			System.out.println("msg");
			System.out.println("collect");
			System.out.println("quit");
			System.out.print("Opcao ->");
			String opcao=sc.nextLine();
			

			switch(opcao) {
			case "store":
				System.out.println("Store:");
				out.writeObject("store");
				store();
				break;
			case "list":
				System.out.println("List:");
				list();
				break;
			case "remove":
				System.out.println("Remove:");
				remove();
				break;
			case "users":
				System.out.println("Users:");
				users();
				break;
			case "trusted":
				System.out.println("Trusted:");
				break;
			case "untrusted":
				System.out.println("Untrusted:");
				untrusted();
				break;
			case "download":
				System.out.println("Donwload:");
				download();
				break;
			case "msg":
				System.out.println("Message:");
				msg();
				break;
			case "collect":
				System.out.println("Collect:");
				collect();
				break;
			case "quit":
				out.writeObject("Quit");
				num=0;
				break;
			default:
				System.out.println("Opcao invalida");
				break;
			}
		}
	}

	public static void store() throws ClassNotFoundException {
		try {

			Boolean verifica = false;
			Boolean fromServer = false;
			String nomedoficheiro="";
			
			while(!verifica) {
				System.out.println("Escreva o(s) nome(s) do(s) ficheiro(s)(ex:ficheiro.txt exemplo.txt):");
				nomedoficheiro = sc.nextLine();
				out.writeObject(nomedoficheiro);

				//faco uma lista dos nomes dos ficheiros
				String[] lista = nomedoficheiro.split(" ");

				//percorro essa lista
				for(int i = 0; i < lista.length; i++) {
					File file = new File(nomedoficheiro);
					out.writeObject((Long)(file.length()));
					InputStream input = new BufferedInputStream(new FileInputStream(file));
					byte[] buffer = new byte[1024];
					int bytesRead;

					while((bytesRead = input.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}

					out.writeObject(name);

					fromServer = (Boolean) in.readObject();

					if(fromServer) {
						System.out.println("O Ficheiro foi adicionado");
					}else {
						System.out.println("Falha ao adicionar o Ficheiro");
					}

				}
				System.out.println("Quer adicionar mais ficheiros?(Sim/Nao)");
				String check=sc.nextLine();
				if(check.equals("sim")) {
					verifica=false;
				}else{
					verifica=true;
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void list() {
		String list = "";
		try {
			out.writeObject("list");
			list = (String) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(list);


	}

	public static void remove() {
		// TODO Auto-generated method stub

	}

	public static void users() {
		String frase="";
		try {
			out.writeObject("users");
			frase= (String) in.readObject();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(frase);
	}

	public static void trusted() {
		try {
			Boolean verifica = false;
			String nomeUser="";

			while(!verifica) {
				System.out.println("Escreva o(s) nome(s) do(s) utilizadores trusted:");
				nomeUser= sc.nextLine();
				
				//faco uma lista dos nomes dos ficheiros
				String[] lista = nomeUser.split(" ");

				//percorro essa lista
				for(int i = 0; i < lista.length; i++) {
					out.writeObject(lista);
				}
				System.out.println("Quer adicionar mais utilizadores?(true/false)");
				
				verifica=sc.nextBoolean();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void untrusted() {

	}

	public static void download() {
		// TODO Auto-generated method stub

	}

	public static void msg() {
		// TODO Auto-generated method stub

	}

	public static void collect() {
		// TODO Auto-generated method stub

	}

}