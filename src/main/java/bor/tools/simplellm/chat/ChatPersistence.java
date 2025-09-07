package bor.tools.simplellm.chat;

/**
 * Define o contrato para mecanismos de persistência de sessões de chat.
 * <p>
 * Esta interface abstrai o armazenamento de objetos {@link Chat}, permitindo que
 * diferentes implementações (como armazenamento em arquivo, banco de dados, etc.)
 * sejam usadas de forma intercambiável. O objetivo é desacoplar a lógica de
 * negócios da aplicação da forma como os dados do chat são salvos.
 * </p>
 *
 * <p><b>Exemplo de implementação (conceitual):</b></p>
 * <pre>{@code
 * public class FileChatPersistence implements ChatPersistence {
 *     private final Path storagePath;
 *
 *     // Construtor e outros métodos...
 *
 *     @Override
 *     public void saveChat(Chat chat) {
 *         // Lógica para serializar e salvar o chat em um arquivo
 *     }
 *
 *     @Override
 *     public Chat loadChat(String chatId) {
 *         // Lógica para encontrar e desserializar o chat de um arquivo
 *         return null;
 *     }
 *
 *     @Override
 *     public void deleteChat(String chatId) {
 *         // Lógica para excluir o arquivo do chat
 *     }
 * }
 * }</pre>
 */
public interface ChatPersistence {

	/**
	 * Salva ou atualiza uma sessão de chat completa no armazenamento.
	 * <p>
	 * Se um chat com o mesmo ID ({@link Chat#getId()}) já existir, a
	 * implementação deve sobrescrevê-lo com o novo estado. Se não existir,
	 * um novo registro deve ser criado.
	 * </p>
	 *
	 * @param chat O objeto {@link Chat} a ser persistido.
	 */
	public void saveChat(Chat chat);
	
	/**
	 * Carrega uma sessão de chat a partir do armazenamento, usando seu identificador único.
	 *
	 * @param chatId O ID único do chat a ser carregado.
	 * @return O objeto {@link Chat} correspondente ao ID fornecido, ou {@code null}
	 *         se nenhum chat for encontrado.
	 */
	public Chat loadChat(String chatId);
	
	/**
	 * Exclui permanentemente uma sessão de chat do armazenamento.
	 * <p>
	 * Se nenhum chat com o ID fornecido for encontrado, a operação pode ser
	 * ignorada silenciosamente pela implementação.
	 * </p>
	 *
	 * @param chatId O ID único do chat a ser excluído.
	 */
	public void deleteChat(String chatId);
	
}
