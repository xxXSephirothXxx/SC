import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Blah

public class Client {
	private List<String> followers;
	private String nome;
	private Map<String,Photo> photos;

	public Client(String nome, boolean existe) {
		this.nome = nome;
		this.photos = new HashMap<>();
		this.followers = new ArrayList<String>();
		//verifica se existe utilizador
		if(!existe) {
			createUser();
		}
		//senao cria
		else {
			try {
				loadUser();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}
		}
	}

	/**
 * Cria um diretorio para o novo utilizador
 */

	private void createUser() {
		new File("Users/"+this.nome).mkdirs();
		new File("Users/"+this.nome + "/photos").mkdirs();
		new File("Users/"+this.nome + "/comments").mkdirs();
		String filename = "Users/"+this.nome + "/seguidores.txt";
		try {
			FileWriter fw = new FileWriter(filename, true);
			fw.write(this.nome+"\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Da load de todos os utilizadores e da sua informação respectiva
	 */

	private void loadUser() throws FileNotFoundException {
		File folder = new File("Users/" + this.nome + "/photos");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {

				photos.put(listOfFiles[i].getName(), new Photo(listOfFiles[i].getName(), this.nome));
			}
		}
		File folderComments = new File("Users/" + this.nome + "/comments");
		File[] listOfComments = folderComments.listFiles();

		Photo temp;
		for (int i = 0; i < listOfComments.length; i++) {
			if (listOfComments[i].isFile()) {
				temp = photos.get(listOfFiles[i].getName());
				BufferedReader buffer = new BufferedReader(new FileReader("Users/" + this.getNome() +"/comments/"+listOfComments[i].getName()));

				String line;
				String []split;
				try {
					line = buffer.readLine();
					split = line.split(":");
					int like = Integer.parseInt(split[1]);
					line = buffer.readLine();
					split = line.split(":");
					int dislike = Integer.parseInt(split[1]);
					line = buffer.readLine();
					String data = line;
					temp.updateValues(like, dislike, data);
					while((line = buffer.readLine())!= null) {
						temp.adicionaComentario(line);
					}
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			BufferedReader buffer = new BufferedReader(new FileReader("Users/" + this.getNome() + "/seguidores.txt"));

			String line;
			while ((line = buffer.readLine()) != null) {
				this.followers.add(line);
			}
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Devolve o nome do utilizador
	 */

	public String getNome() {
		return this.nome;
	}

	/**
	 * Devolve os fotos do utilizador
	 */

	public Collection<Photo> getPhotos(){
		return this.photos.values();
	}

	/**
	 * Devolve os followers do utilizador
	 */

	public Iterable<String> getFollowers() {
		return this.followers;
	}

	/**
	 * Adiciona uma nova foto no reportorio do utilizador
	 * @param nome nome para a foto que vai ser adicionada
	 * @param data data para a foto que vai ser adicionada
	 */

	public boolean addPhoto(String nome, String data) {
		Photo foto = new Photo(nome, this.nome);
		StringBuilder comment = new StringBuilder();
		comment.append(nome);
		comment.replace(nome.length()-4, nome.length(), ".txt");
		String filename = "Users/"+this.nome + "/comments/"+ comment;
		try {
			FileWriter fw = new FileWriter(filename, true);
			fw.write("Likes:0\nDislikes:0\n");
			fw.write(data+"\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		foto.setData(data);
		this.photos.put(nome, foto);
		return true;
	}

	/**
	 * Verifica se e seguidor ou nao
	 * @param nome nome a verificar se é seguidor
	 */

	public boolean isFollower(String nome) {
		return this.followers.contains(nome);
	}

	/**
	 * Verifica se uma determinada photo existe
	 * @param string nome da foto a verificar se existe
	 */

	public boolean isPhoto(String string) {
		return photos.containsKey(string);
	}

	/**
	 * @param nome nome a adicionar como follower
	 */

	public boolean addFollower(String nome) {
		return this.followers.add(nome);
	}

	/**
	 * @param nome nome a remover como follower
	 */

	public boolean removeFollower(String nome) {
		return this.followers.remove(nome);
	}

	/**
	 * Devolve a foto com o determinado nome
	 * @param foto nome da foto que tem de ser retornada
	 */

	public Photo getPhoto(String foto) {
		return this.photos.get(foto);
	}

	/**
	 * Adiciona um like a uma determinada foto
	 * @param string nome da foto a qual vais ser adicionada o like
	 */

	public void addLike(String string) {
		photos.get(string).addLike();
	}

	/**
	 * Retorna os likes a uma determinada foto
	 * @param string nome da foto sobre a qual se quer os likes
	 */

	public int getLikes(String string) {
		return photos.get(string).getLikes();
	}

	/**
	 * Adiciona um dislike a uma determinada foto
	 * @param string nome da foto a qual vais ser adicionada o dislike
	 */

	public void addDislike(String string) {
		photos.get(string).addDislike();
	}

	/**
	 * Retorna os dislikes a uma determinada foto
	 * @param string nome da foto sobre a qual se quer os dislikes
	 */

	public int getDislikes(String string) {
		return photos.get(string).getDislikes();
	}

	/**
	 * Adiciona um comentario a uma determinada foto
	 * @param foto nome da foto a qual vais ser adicionada o comentario
	 * @param comentario comentario que vai ser adicionado na foto
	 */

	public void addComment(String foto, String comentario) {
		this.photos.get(foto).adicionaComentario(comentario);
	}

	/**
	 * Devolve a lista das fotos do utilizador
	 */

	public String getListaFotos() {
		StringBuilder sb = new StringBuilder();
		Photo temp;
		for(Map.Entry<String, Photo> entry : this.photos.entrySet()) {
		    temp = entry.getValue();
			sb.append(temp.toString()+"-----------\n");
		}
		return sb.toString();
	}

	/**
	 * Devolve a informacao relativa a uma determinada foto
	 * @param foto nome da foto sobre a qual queremos informacao
	 */

	public String getPhotoInfo(String foto) {
		return this.photos.get(foto).getInfo();
	}
}
