import java.util.LinkedList;
import java.util.List;

public class Photo {
	private String path;
	private String nome;
	private String user;
	private String data;
	private int likes;
	private int dislikes;
	private List<String> comentarios;

	public Photo(String nome, String user) {
		this.user = (user);
		this.nome = nome;
		this.path = ("Users/" + user + "/photos/" + nome);
		this.likes = 0;
		this.dislikes = 0;
		this.comentarios = new LinkedList<>();
	}

	/**
	 * Devolve o caminho
	 */

	public String getPath() {
		return this.path;
	}

	/**
	 * Devolve o nome da foto
	 */

	public String getNome() {
		return this.nome;
	}

	/**
	 * Devolve o numero de dislikes
	 */

	public int getLikes() {
		return this.likes;
	}

	/**
	 * Devolve o numero de dislikes
	 */

	public int getDislikes() {
		return this.dislikes;
	}

	/**
	 * Devolve o nome do utilizador
	 */

	public String getUser() {
		return user;
	}

	/**
	 * Adiciona um comentario
	 * @param comment comentario a adicionar
	 */

	public void adicionaComentario(String comment) {
		this.comentarios.add(comment);
	}

	/**
	 * Retorna os comentarios num array
	 */

	public String[] getComentarios() {
		return this.comentarios.toArray(new String[0]);
	}

	/**
	 * Incrementa o numero de likes
	 */

	public void addLike() {
		this.likes++;

	}

	/**
	 * Incrementa o numero de dislikes
	 */

	public void addDislike() {
		this.dislikes++;

	}

	/**
	 * Retorna a data
	 */

	public String getData() {
		return data;
	}

	/**
	 * Dá update do numero de likes e dislikes e da data
	 * @param numero de likes
	 * @param numero de dislikes
	 * @param string com a data da foto
	 */

	public void updateValues(int like, int dislike, String line) {
		this.likes = like;
		this.dislikes = dislike;
		this.data = line;
	}

	/**
	 * Prepara para enviar para o cliente o output
	 */

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Nome:" + this.nome + "\n" + "Data:" + this.data + "\n");
		return sb.toString();
	}

	/**
	 * Atribui um valor à data
	 * @param data  data a atribuir
	 */

	public void setData(String data) {
		this.data=data;
	}

	/**
	 * Prepara para enviar para o cliente a informacao dos likes e dislikes e os comentarios de uma foto
	 */

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("Likes:" + this.likes + "\n" +"Dislikes:"+this.dislikes+"\nComentarios:\n");
		for(String comentario : this.comentarios) {
			sb.append(comentario);
		}
		return sb.toString();
	}

}
