
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class MsgFileServer{

	static int socketServer;
	static CatalogoClientes clientes;


	public static void main(String[] args) throws IOException {
		clientes= new CatalogoClientes();
		Scanner sc= new Scanner(System.in);
		socketServer= selSocket(args[0]);
		MsgFileServer server = new MsgFileServer();
		server.startServer(socketServer);
	}

	private static int selSocket(String frase) {
		String[] palavras= frase.split("<");
		String[] palavras2= palavras[1].split(">");
		return Integer.parseInt(palavras2[0]);
	}

	public void startServer (int socketServer){
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(socketServer);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		System.out.println("Servidor Ligado");

		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		//sSoc.close();
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;
		private ObjectOutputStream outStream;
		private ObjectInputStream inStream;
		private Cliente currUser = null;
		private int valor = 0;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}

		public void run(){


			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;
				String comando = null;

				try {
					user = (String)inStream.readObject();
					System.out.println("User:" + user);
					passwd = (String)inStream.readObject();
					System.out.println("Thread: Recebeu o username e a password");
					valor=clientes.verificar(user,passwd);
				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				//Se o cliente existe
				if(valor==2) {
					clientes.getUser(user);
					outStream.writeObject(new Boolean(true));
					System.out.println("Client Verified");

					//se nao existe, eh criado
				} else if(valor==0) {
					Cliente novo= new Cliente(user,passwd);
					currUser=novo;
					clientes.addCliente(novo);
					outStream.writeObject(new Boolean(true));
					System.out.println("Client Created");

				} else {
					do{
						outStream.writeObject(new Boolean(false));
						passwd=(String) inStream.readObject();
					} while(clientes.verificar(user,passwd)==1);
					clientes.getUser(user);
					outStream.writeObject(new Boolean(true));
				}

				System.out.println("Cliente autenticado!");
				
				while (true) {
					comando = (String) inStream.readObject();
					if(comando.equals("quit")) {
						System.out.println("O cliente " + this.currUser.getUsername() + " fez logout");
						break;
					}
					processComand(comando);
				}
				outStream.close();
				inStream.close();
				socket.close();
			}catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}


		public void processComand(String comando) {	
			switch(comando){
			case "store":
				store();
				break;
			case "list":
				list();
				break;
			case "remove":
				break;
			case "users":
				users();
				break;
			case "trusted":
				break;
			case "untrusted":
				break;
			case "download":
				break;
			case "msg":
				break;
			case "collect":
				break;
			default:
				System.out.println("Opcao invalida");
				break;

			}
		}

		private void store() {
			try {
				Boolean verificador=true;
				while(verificador) {
					String nomeFicheiro=(String) inStream.readObject();
					if(!(currUser.getListaFicheiros().contains(nomeFicheiro))) {
						long sizeFile = 0;

						try {
							sizeFile = (Long) inStream.readObject();
							System.out.println("Recebeu o size");
						}catch (IOException e) {
							e.printStackTrace();
						} 		

						byte [] b = new byte[1024];
						File file = new File(nomeFicheiro);

						OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
						while(inStream.read(b, 0, b.length) != -1) {
							output.write(b, 0, b.length);
						}
						output.flush();

						if( file.length() == 0)
							outStream.writeObject(new Boolean(false));
						else {
							outStream.writeObject(new Boolean(true));
						}
						file.renameTo(new File("./"+currUser.getUsername()));
						currUser.addFicheiro(nomeFicheiro, file);
					}else {
						outStream.writeObject(new Boolean(false));
					}
					Boolean fromServer = (Boolean) inStream.readObject();
					if(fromServer==true) {
						verificador=true;
					}else {
						verificador=false;
					}
				}

				outStream.close();
				inStream.close();

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void list() {
			ArrayList<String> lista= currUser.getListaFicheiros();
			try {
				outStream.writeObject(lista.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void remove(String nomeFicheiro) {

		}

		private void users() {
			StringBuilder lista=clientes.getusers();
			try {
				outStream.writeObject(lista.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void trusted(Cliente nomeUti) {
			if(!(currUser.getSeguidores().contains(nomeUti.getUsername())) && 
					currUser.isTrusted(nomeUti.getUsername())) {

				try {
					currUser.addSeguidor(nomeUti.getUsername());
					System.out.println("O seguidor foi adicionado");
					outStream.writeObject(new Boolean(true));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					outStream.writeObject(new Boolean (false));
					System.out.println("Erro ao adicionar" + nomeUti.getUsername());
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		private void untrusted(Cliente nomeUti) {
			if(currUser.getSeguidores().contains(nomeUti.getUsername()) 
					&& currUser.isUntrusted(nomeUti.getUsername())) {

				try {
					currUser.removeSeguidor(nomeUti.getUsername());
					System.out.println("O seguidor foi removido");
					outStream.writeObject(new Boolean(true));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					outStream.writeObject(new Boolean (false));
					System.out.println("Erro ao eliminar" + nomeUti.getUsername());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void download(Cliente nomeUti, String nomeFicheiro) {
			if((currUser.getUsername().equals(nomeUti.getUsername()))) {
				try {
					outStream.writeObject(new Boolean (false));
					System.out.println("O utilizador nao eh valido");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if(nomeUti.listaFicheiros.containsKey(nomeFicheiro) && 
					currUser.getSeguidores().contains(nomeUti)) {
				nomeUti.getFicheiro(nomeFicheiro);
				//FALTAM COISAS
			} else {
				try {
					outStream.writeObject(new Boolean (false));
					System.out.println("Nao foi possivel obter o ficheiro" +
							nomeFicheiro + " do utilizador " + nomeUti);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}	
		}

		public void msg(Cliente nomeUti, String mensagem) {
			if(currUser.equals(nomeUti)) {
				try {
					outStream.writeObject(new Boolean (false));
					System.out.println("Nao foi possivel enviar a mensagem para o Cliente" +
							nomeUti.getUsername() + "pois este eh o utilizador local");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if(clientes.getUser(nomeUti.getUsername())==null) {
				try {
					outStream.writeObject(new Boolean (false));
					System.out.println("Nao foi possivel enviar a mensagem para o Cliente" +
							nomeUti.getUsername() + "pois o Cliente nao existe");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if((currUser.getUsername().equals(nomeUti.getUsername())) ||
					!(nomeUti.getSeguidores().contains(currUser.getUsername()))) {

			}
		}

	}
}