
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class PhotoShareServer {
	private Map<String,Client> clientes;

	public static void main(String[] args) throws IOException {

		int port = Integer.parseInt(args[0]);
		PhotoShareServer server = new PhotoShareServer();
		server.startServer(port);
	}

	/**
	 * Cria os ficheiros e as pastas necessarias ou, caso ja existam, faz load
	 * dos utilizadores e deixa o servidor pronto a receber pedidos dos clientes
	 * @param port porta que recebe ligacoes dos clientes
	 */
	public void startServer(int port) {
		ServerSocket sSoc = null;
		clientes = new HashMap<>();
		boolean existe = false;
		File f = null;
		try {
			// testar se ja existe users.txt
			f = new File("users.txt");

			existe = f.exists();
			if (!existe) {
				f.createNewFile();
				new File("Users").mkdirs();
			}
			else { //foi abaixo
				BufferedReader buffer = new BufferedReader(new FileReader("users.txt"));
				Client tempUser;
				String[] temp = new String[2];
				String line;
				while ((line = buffer.readLine()) != null) {
					temp = line.split(":");
					tempUser = new Client(temp[0],true);
					clientes.put(temp[0],tempUser);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// sSoc.close();
	}

	// Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;
		private Client currUser = null;
		private ObjectOutputStream outStream = null;
		private ObjectInputStream inStream = null;
		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("Thread do cliente criada!");
			try {
				 outStream = new ObjectOutputStream(socket.getOutputStream());
				 inStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		/**
		 * Metodo onde vai estar a correr o cliente 
		 */
		public void run() {
			try {
				String user = null;
				String passwd = null;
				String comando = null;
				try {
					user = (String) inStream.readObject();
					System.out.println("Utilizador: "+user);
					passwd = (String) inStream.readObject();

				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				BufferedReader buffer = new BufferedReader(new FileReader("users.txt"));
				String line;
				boolean existe = false;
				boolean autenticado = false;
				String[] temp = new String[2];
				while ((line = buffer.readLine()) != null) {
					temp = line.split(":");
					if (temp[0].equals(user)) {
						existe = true;
						if (temp[1].equals(passwd)) {
							autenticado = true;
							currUser = clientes.get(temp[0]);
							break;
						} else { // palavra pass errada TODO:
							System.out.println("O cliente inseriu a pwd errada");
							break;
						}
					}
				}
				// se nao existe
				if (!existe) {
					try {
						String filename = "users.txt";
						FileWriter fw = new FileWriter(filename, true);
						String userPass = (user + ":" + passwd);
						fw.write(userPass + "\n");
						fw.close();
						existe = true;
						autenticado = true;
						currUser = new Client(user, false);
						clientes.put(user, currUser);
						currUser.addFollower(user);
						clientes.put(user, currUser);
					} catch (IOException ioe) {
						System.err.println("IOException: " + ioe.getMessage());
					}
				}
				if (autenticado) {
					outStream.writeObject(new Boolean(true));
				} else { // existe mas nao autenticado
					while (!passwd.equals(temp[1])) {
						outStream.writeObject(new Boolean(false));
						try {
							passwd = (String) inStream.readObject();
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						}
					}
					currUser = clientes.get(user);
					outStream.writeObject(new Boolean(true));
				}

				System.out.println("Cliente autenticado!");
				while (true) {
					comando = (String) inStream.readObject();
					if(comando.equals("quit")) {
						System.out.println("O cliente " + this.currUser.getNome() + " fez logout");
						break;
					}
					processComand(comando);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		/**
		*Processa o comando inserido pelo utilizador na linha de comandos
		*@param comando comando inserido pelo user
		*/

		private void processComand(String comando) {
			String[] op = comando.split(" ");
			switch (op[0]) {
			case "-a":
				adicionaFotos(op);
				break;
			case "-L":
				if(adicionaLike(op)) {
					try {
						outStream.writeObject(new Boolean(true));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						outStream.writeObject(new Boolean(false));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			case "-D":
				if(adicionaDislike(op)) {
					try {
						outStream.writeObject(new Boolean(true));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						outStream.writeObject(new Boolean(false));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			case "-f":
				adicionaSeguidores(op);
				break;
			case "-c":
				if(adicionaComentario(op)) {
					try {
						outStream.writeObject(new Boolean(true));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						outStream.writeObject(new Boolean(false));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			case "-r":
				removeSeguidores(op);
				break;
			case "-l":
				devolveLista(op);
				break;
			case "-i":
				devolveInformacao(op);
				break;
			case "-g":
				copiaFotos(op);
				break;
			default:
				devolveErro();
				break;
			}

		}
		
		/**
		 * Copia as fotos de um certo utilizador para a maquina do cliente que
		 * faz o pedido
		 * @param op comando 
		 */
		private void copiaFotos(String[] op) {
			Client clienteTemp = null;
			if (clientes.containsKey(op[1]))
				clienteTemp = clientes.get(op[1]);
			else {
				try {
					outStream.writeObject(new Boolean(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			if (clienteTemp.isFollower(currUser.getNome())) {
				StringBuilder sb = new StringBuilder();
				for (Photo foto : clienteTemp.getPhotos()) {
					sb.append(foto.getNome()+" ");
				}

				try {
					outStream.writeObject(new Boolean(true));
					outStream.writeObject(sb.toString());
				} catch (IOException e) {

					e.printStackTrace();
				}
				for (Photo foto : clienteTemp.getPhotos()) {

					try {
						if(((Boolean) inStream.readObject()).booleanValue()) {
							File f = new File(foto.getPath());
							byte[] content = Files.readAllBytes(f.toPath());
							outStream.writeObject(content);
							TimeUnit.SECONDS.sleep(1);
							File fc = new File("Users/"+clienteTemp.getNome()+"/comments/"+foto.getNome().substring(0, foto.getNome().length()-4).concat(".txt"));
							byte[] contentc = Files.readAllBytes(fc.toPath());
							outStream.writeObject(contentc);
							System.out.println(foto.getNome()+" foi enviado");
						}
						else
							System.out.println("Erro ao enviar: " + foto.getNome());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {

						e.printStackTrace();
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}

			} else {
				try {
					outStream.writeObject(new Boolean(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		/**
		 * Devolve os likes, dislikes e comentarios da foto de um utilizador 
		 * @param op comando
		 */
		private void devolveInformacao(String[] op) {
			Client clienteTemp = null;
			if (clientes.containsKey(op[1]))
				clienteTemp = clientes.get(op[1]);
			else {
				try {
					outStream.writeObject(new Boolean(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			if (clienteTemp.isFollower(currUser.getNome())) {
				try {
					outStream.writeObject(new Boolean(true));
					if(clienteTemp.isPhoto(op[2])) {
						outStream.writeObject(clienteTemp.getPhotoInfo(op[2]));
					}
					else {
						outStream.writeObject("A foto nao existe! \n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			else {
				try {
					outStream.writeObject(new Boolean(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
		/**
		 * Devolve a lista de fotos de um utilizador, mostrando o nome e data de publicacao
		 * @param op comando
		 */
		private void devolveLista(String[] op) {
			Client clienteTemp = null;
			if (clientes.containsKey(op[1]))
				clienteTemp = clientes.get(op[1]);
			else {
				try {
					outStream.writeObject(new Boolean(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			if (clienteTemp.isFollower(currUser.getNome())) {
				try {
					outStream.writeObject(new Boolean(true));
				} catch (IOException e) {
					e.printStackTrace();
				}
				String result = clienteTemp.getListaFotos();
				if (result == null || result.equals("")) {
					try {
						outStream.writeObject("Ainda n�o tem fotos!\n");
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				} else {
					try {
						outStream.writeObject(result);
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}

			}
			else {
				try {
					outStream.writeObject(new Boolean(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		/**
		 * Adiciona um dislike a uma foto de um utilizador
		 * @param op comando
		 */
		private boolean adicionaDislike(String[] op) {
			currUser = clientes.get(currUser.getNome());
			Client clienteTemp;
			if(clientes.containsKey(op[1]))
				clienteTemp = clientes.get(op[1]);
			else
				return false;
			String nome_foto = op[2];
			String path = nome_foto.substring(0, nome_foto.length()-4);
			path = path.concat(".txt");
			if(clienteTemp.isFollower(currUser.getNome()) && clienteTemp.isPhoto(op[2]) ) {
				clienteTemp.addDislike(op[2]);
				int dislikes = clienteTemp.getDislikes(op[2]);
				try {
					BufferedReader br = new BufferedReader(new FileReader("Users/"+clienteTemp.getNome()+"/comments/"+path));

					StringBuilder sb = new StringBuilder();
					sb.append(br.readLine()+"\n");
					sb.append("Dislikes:"+dislikes+"\n");
					br.readLine();
					String line;
					while ((line = br.readLine()) != null) {
					    sb.append(line+"\n");
					}
					BufferedWriter bw = new BufferedWriter(new FileWriter("Users/"+clienteTemp.getNome()+"/comments/"+path));
					bw.write(sb.toString());
					bw.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				clientes.put(clienteTemp.getNome(), clienteTemp);
				return true;
			}
			else {
				return false;
			}

		}
		
		/**
		 * Adiciona um like a uma foto de um utilizador
		 * @param op comando
		 */
		private boolean adicionaLike(String[] op) {
			currUser = clientes.get(currUser.getNome());
			Client clienteTemp;
			if(clientes.containsKey(op[1]))
				clienteTemp = clientes.get(op[1]);
			else
				return false;
			String nome_foto = op[2];
			String path = nome_foto.substring(0, nome_foto.length()-4);
			path = path.concat(".txt");
			if(clienteTemp.isFollower(currUser.getNome()) && clienteTemp.isPhoto(op[2]) ) {
				clienteTemp.addLike(op[2]);
				int likes = clienteTemp.getLikes(op[2]);

				try {
					BufferedReader br = new BufferedReader(new FileReader("Users/"+clienteTemp.getNome()+"/comments/"+path));

					StringBuilder sb = new StringBuilder();
					sb.append("Likes:"+likes+"\n");
					br.readLine();
					String line;
					while ((line = br.readLine()) != null) {
					    sb.append(line+"\n");
					}
					BufferedWriter bw = new BufferedWriter(new FileWriter("Users/"+clienteTemp.getNome()+"/comments/"+path));
					bw.write(sb.toString());
					bw.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				clientes.put(clienteTemp.getNome(), clienteTemp);

				return true;
			}
			else
				return false;
		}
		
		/**
		 * Adiciona as fotos enviadas e o respetivo ficheiro .txt ao diretorio do utilizador 
		 * @param op comando
		 */
		private boolean adicionaFotos(String[] op) {
			currUser = clientes.get(currUser.getNome());
			for (int i = 1; i < op.length; i++) {
				if(!currUser.isPhoto(op[i])) {
					try {
						outStream.writeObject(new Boolean(true));
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					File img = null;
					try {
						 img = new File("Users/" + currUser.getNome() + "/photos/" + op[i]);
						 byte[] content = (byte[])inStream.readObject();
						 Files.write(img.toPath(), content);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					System.out.println("Ficheiro recebido!");
					DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					Date date = new Date();

					currUser.addPhoto(op[i],dateFormat.format(date));
					clientes.put(currUser.getNome(), currUser);
				}
				else {
					try {
						outStream.writeObject(new Boolean(false));
						System.out.println("Erro ao adicionar: "+ op[i]);
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			}
			return true;
		}
		
		/**
		 * Adiciona um comentario a uma foto de um utilizador
		 * @param op comando
		 */
		private boolean adicionaComentario(String[] op) {
			currUser = clientes.get(currUser.getNome());
			Client clienteTemp;;
			String seguidor = op[op.length-2];
			String nome_foto = op[op.length-1];
			String path = nome_foto.substring(0, nome_foto.length()-4);
			path = path.concat(".txt");
			if(clientes.containsKey(seguidor))
				clienteTemp = clientes.get(seguidor);
			else
				return false;

			StringBuilder sb = new StringBuilder();
			for(int i = 1 ; i < op.length-2; i++) {
				sb.append(op[i]+ " ");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
			String comentario = sb.toString();

			if(clienteTemp.isFollower(this.currUser.getNome()) && clienteTemp.isPhoto(nome_foto)) {
				try {
					FileWriter fw = new FileWriter("Users/"+ clienteTemp.getNome()+"/comments/"+path,true);
					fw.write(comentario+"\n");
					fw.close();
					clienteTemp.addComment(nome_foto, comentario);
					clientes.put(clienteTemp.getNome(), clienteTemp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				return false;
			}
			return true;
		}
		
		/**
		 * Adiciona seguidores ah lista de seguidores do utilizador
		 * @param op comando
		 */
		private boolean adicionaSeguidores(String[] op) {
			currUser = clientes.get(currUser.getNome());
			for(int i = 1; i< op.length; i++) {
				if(!currUser.isFollower(op[i]) && clientes.containsKey(op[i])) {
					try {

						FileWriter out = new FileWriter("Users/"+currUser.getNome()+"/seguidores.txt",true);
						out.write(op[i]+"\n");
						this.currUser.addFollower(op[i]);
						clientes.put(currUser.getNome(), currUser);
						System.out.println("Seguidor adicionado");
						out.close();
						outStream.writeObject(new Boolean(true));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				else {
					try {
						outStream.writeObject(new Boolean(false));
						System.out.println("Erro ao adicionar: "+ op[i]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		}
		
		/**
		 * Remove os seguidores desejados da lista de seguidores
		 * @param op comando
		 */
		private boolean removeSeguidores(String[] op) {
			currUser = clientes.get(currUser.getNome());
			for (int i = 1; i < op.length; i++) {
				if (currUser.isFollower(op[i])) {
					try {
						outStream.writeObject(new Boolean(true));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					currUser.removeFollower(op[i]);
					try {
						BufferedReader br = new BufferedReader(new FileReader("Users/" + currUser.getNome() + "/seguidores.txt"));
						StringBuilder sb = new StringBuilder();
						String line;
						while ((line = br.readLine()) != null) {
							if (!line.equals(op[i])) {
								sb.append(line + "\n");
							}

						}
						BufferedWriter bw = new BufferedWriter(
								new FileWriter("Users/" + currUser.getNome() + "/seguidores.txt"));
						bw.write(sb.toString());
						bw.close();
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Seguidor " + op[i] + " removido!");

				} else {
					try {
						outStream.writeObject(new Boolean(false));
						System.out.println("Erro ao remover: " + op[i]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			return true;
		}

		private void devolveErro() {
			// TODO Auto-generated method stub

		}
	}
}
