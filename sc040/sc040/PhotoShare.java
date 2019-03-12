import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class PhotoShare {

	public static void main(String[] args) throws IOException {
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String localUserId = null;
		String password = null;
		String[] serveraddress = null;
		String ip = null;
		String port = null;
		String comando = "default";

		if (args.length == 3) {
			localUserId = args[0];
			password = args[1];
			serveraddress = args[2].split(":");
			ip = serveraddress[0];
			port = serveraddress[1];
		} else if (args.length == 2) {
			localUserId = args[0];
			serveraddress = args[1].split(":");
			ip = serveraddress[0];
			port = serveraddress[1];
			System.out.println("Insira a password");
			password = stdIn.readLine();

		} else {
			System.out.println("Uso: PhotoShare <localUserId> <password> <serverAddress>");
			System.exit(1);
		}

		try (Socket socket = new Socket(ip, Integer.parseInt(port));
				// para transferencia de Objects
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());) {
			String fromServer;
			//Efetuar login
			outStream.writeObject(localUserId);
			System.out.println("Username:" + localUserId);
			outStream.writeObject(password);
			System.out.println("Password:" + password);
			try {
				boolean bool = ((Boolean) inStream.readObject()).booleanValue();

				while (!bool) {
					System.out.println("pass incorreta! Try again!");
					outStream.writeObject(stdIn.readLine());
					bool = ((Boolean) inStream.readObject()).booleanValue();

				}
				System.out.println("Login feito");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			comando = stdIn.readLine();

			//trata todos as accoes possiveis de fazer pelo utilizador e da feedback de retorno
			while (!comando.equals("quit")) {
				System.out.println(comando);
				String[] op = comando.split(" ");
				if(!validaComando(op)) {
				 System.out.println("Comando invalido, verifique todos os argumentos");
				 op[0] = "invalido";
				 }
				switch (op[0]) {
				case "-a":
					outStream.writeObject(comando);
					try {
						for (int i = 1; i < op.length; i++) {
							if (((Boolean) inStream.readObject()).booleanValue()) {
								File f = new File(op[i]);
								byte[] content = Files.readAllBytes(f.toPath());
								outStream.writeObject(content);
								TimeUnit.SECONDS.sleep(1);
								System.out.println(op[i] + " foi adicionado");
							} else
								System.out.println("Erro ao adicionar: " + op[i]);
						}
						System.out.println("Opera��o terminada");
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "-c":
					outStream.writeObject(comando);
					if (((Boolean) inStream.readObject()).booleanValue()) {
						System.out.println("O comentario foi adicionado com sucesso!");
					} else
						System.out.println("Utilizador nao eh seguidor");
					System.out.println("Opera��o terminada");
					break;
				case "-L":
					outStream.writeObject(comando);
					if (((Boolean) inStream.readObject()).booleanValue()) {
						System.out.println("O like foi adicionado com sucesso!");
					} else
						System.out.println("Utilizador nao eh seguidor");
					System.out.println("Opera��o terminada");
					break;
				case "-D":
					outStream.writeObject(comando);
					if (((Boolean) inStream.readObject()).booleanValue()) {
						System.out.println("O dislike foi adicionado com sucesso!");
					} else
						System.out.println("Utilizador nao eh seguidor");
					System.out.println("Opera��o terminada");
					break;
				case "-f":
					outStream.writeObject(comando);
					for (int i = 1; i < op.length; i++) {
						if (((Boolean) inStream.readObject()).booleanValue()) {
							System.out.println("O " + op[1] + " foi adicionado como seguidor!");
						} else
							System.out.println("Erro ao adicionar " + op[i] + " como seguidor");
					}
					System.out.println("Opera��o terminada");
					break;
				case "-r":
					outStream.writeObject(comando);
					for (int i = 1; i < op.length; i++) {
						if (((Boolean) inStream.readObject()).booleanValue()) {
							System.out.println("O " + op[i] + " foi removido como seguidor!");
						} else
							System.out.println("Erro ao remover " + op[i] + " como seguidor");
					}
					System.out.println("Opera��o terminada");
					break;
				case "-l":
					outStream.writeObject(comando);
					if (((Boolean) inStream.readObject()).booleanValue()) {
						System.out.print((String) inStream.readObject());
					} else
						System.out.println("Utilizador nao eh seguidor");
					System.out.println("Opera��o terminada");
					break;
				case "-i":
					outStream.writeObject(comando);
					if (((Boolean) inStream.readObject()).booleanValue()) {
						System.out.print((String) inStream.readObject());
					} else
						System.out.println("Utilizador nao eh seguidor");
					System.out.println("Opera��o terminada");
					break;
				case "-g":
					outStream.writeObject(comando);
					if (((Boolean) inStream.readObject()).booleanValue()) {
						String nomes = (String) inStream.readObject();
						String[] nomesSplit = nomes.split(" ");
						if (nomes.equals("")) {
							System.out.println("O utilizador n�o tem fotos!");
						} else {
							for (int i = 0; i < nomesSplit.length; i++) {
								try {
									outStream.writeObject(new Boolean(true));
								} catch (IOException e2) {
									e2.printStackTrace();
								}
								File img = null;
								File info = null;
								try {
									img = new File(nomesSplit[i]);
									byte[] content = (byte[]) inStream.readObject();
									Files.write(img.toPath(), content);
									info = new File(
											nomesSplit[i].substring(0, nomesSplit[i].length() - 4).concat(".txt"));
									byte[] comentarios = (byte[]) inStream.readObject();
									Files.write(info.toPath(), comentarios);
									System.out.println("A foto " + nomesSplit[i] + " foi recebida!");
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}
					} else {
						System.out.println("N�o � seguidor do utilizador");
					}
					System.out.println("Opera��o terminada");
					break;
				default:
					System.out.println("Uso: [ -a <photos> | -l <userId>  | -i <userId> <photo> | -g <userId> |  "
							+ "     -c <comment> <userId> <photo> | -L <userId> <photo> |     -D <userId> <photo> |"
							+ " -f <followUserIds>  | -r <followUserIds> ]");
					break;
				}
				comando = stdIn.readLine();
			}
			outStream.writeObject(comando); // avisa que vai sair
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + "23232");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + "23232");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verifica se o comando inserido pelo utilizador tem os argumentos corretos
	 * @param comando comando inserido pelo utilizador
	 */

	private static boolean validaComando(String[] comando) {
		switch (comando[0]) {
		case "-a":
			if (comando.length < 2)
				return false;
			boolean test = true;
			for (int i = 1; i < comando.length; i++) {
				File f = new File(comando[i]);
				if (f.exists())
					break;
				else
					test = false;
			}
			return test;
		case "-l":
			if (comando.length == 2)
				return true;

			else
				return false;
		case "-i":
			if (comando.length == 3) {
				return true;
			}

			else
				return false;
		case "-g":
			if (comando.length == 2)
				return true;

			else
				return false;
		case "-c":
			if (comando.length >=4) {
				return true;
			}
			else
				return false;
		case "-L":
			if (comando.length == 3) {
				return true;
			}
			else
				return false;
		case "-D":
			if (comando.length == 3) {
				return true;
			} else
				return false;
		case "-f":
			if (comando.length >= 2)
				return true;

			else
				return false;
		case "-r":
			if (comando.length >= 2)
				return true;

			else
				return false;
		default:
			return false;
		}
	}

}
