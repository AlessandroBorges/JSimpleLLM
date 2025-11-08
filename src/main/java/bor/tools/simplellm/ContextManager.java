package bor.tools.simplellm;

import java.util.ArrayDeque;
import java.util.Deque;

import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.chat.MessageRole;
import bor.tools.simplellm.exceptions.LLMException;
import lombok.Getter;
import lombok.Setter;

/**
 * Class to manage LLM ContextWindow.
 * The goals are:
 * <li>Keep the input Chat as it is.
 * <li>Apply policy to make sure the outputChat context fits within available
 * context.
 * <br>
 * The available policies are:
 * <li>ROLLING_WINDOW,
 * <li>CUT_MIDDLE,
 * <li>MEMORY_BLOCKS
 * <li>SUMMARIZATION
 */
@Getter
@Setter
public class ContextManager {

	private static final String PROMPT_SUMMARIZE =
	            "Summarize briefly the following conversation between a user and an AI assistant:";

	public enum ContextWindowType {
			ROLLING_WINDOW, CUT_MIDDLE, MEMORY_BLOCKS, SUMMARIZATION
	}

	/**
	 * The type of context window management strategy to be applied.
	 * Defaults to {@code ROLLING_WINDOW}.
	 * 
	 * @see ContextWindowType
	 */
	private ContextWindowType contextType = ContextWindowType.ROLLING_WINDOW;

	/**
	 * The original chat input before context management is applied.
	 */
	private Chat inputChat;

	/**
	 * Chat with all messages compressed
	 */
	private Chat compressedChat;

	/**
	 * The resulting chat after applying the context management strategy.
	 */
	private Chat outputChat;
	/**
	 * The maximum number of tokens allowed in the context window.
	 */
	private int  maxContextWindow     = 4096;
	/**
	 * The minimum buffer of tokens that should remain free in the context window
	 * after adding new messages.
	 */
	private int  minimumContextWindow = 512;

	/**
	 * Serviço de LLM para operações como sumarização e token count.
	 * 
	 * @see LLMProvider#sumarizeChat(Chat, String, MapParam)
	 * @see LLMProvider#tokenCount(String, String)
	 */
	private LLMProvider llmService;

	public ContextManager() {}

	public ContextManager(ContextWindowType contextType, int maxContextWindow, LLMProvider llmService) {
		super();
		this.contextType = contextType;
		this.maxContextWindow = maxContextWindow;
		this.llmService = llmService;
	}

	public void applyContextManagement(Chat input, Chat output)
	            throws LLMException {
		this.inputChat = input;
		this.outputChat = output;
		switch (contextType) {
			case ROLLING_WINDOW:
				applyRollingWindow();
				break;
			case CUT_MIDDLE:
				applyCutMiddle();
				break;
			case MEMORY_BLOCKS:
				applyMemoryBlocks();
				break;
			case SUMMARIZATION:
				applySummarization();
				break;
			default:
				applyRollingWindow();
				break;
		}
	}

	/**
	 * Implementações dos diferentes tipos de gerenciamento de contexto.
	 * Cada método deve ajustar o outputChat com base no inputChat e na
	 * estratégia de gerenciamento de contexto selecionada.
	 */
	protected void applyHierarchical() {

	}

	/**
	 * Compress all previous chats
	 * 
	 * @throws LLMException
	 */
	protected void applySummarization()
	            throws LLMException {
		if (llmService != null) {
			if (compressedChat == null || compressedChat.size() == 0) {
				compressedChat = llmService.sumarizeChat(inputChat, PROMPT_SUMMARIZE, null);
			} else {
				// add missing chats
				for (Message m : inputChat.getMessages()) {
					Message outputM = compressedChat.findMessage(m.getIdMessage());
					if (outputM == null) {
						// must add message
						if (m.getRole() == MessageRole.SYSTEM) {
							outputChat.addMessage(m);
						} else if (ContentType.TEXT.equals(m.getContent().getType())) {
							// missing Message
							String  txt  = llmService.sumarizeText(m.getText(), PROMPT_SUMMARIZE, null);
							Message nova = m.clone();
							nova.setText(txt);
							compressedChat.addMessage(nova);
						}
					}
				} // for
			} // end synchonization
		} // end compression

	}

	protected void applyMemoryBlocks() {
		// TODO Auto-generated method stub

	}

	protected void applyCutMiddle() {
		// TODO Auto-generated method

	}

	/**
	 * Aplica a estratégia de janela deslizante (rolling window) para manter o
	 * contexto. <br>
	 * Deve preservar:
	 * <ul>
	 * <ui> as mensagens do tipo SYSTEM,
	 * <ui> as mais recentes dentro do limite de tokens
	 * <ul>
	 * Se uma única mensagem exceder o 80% do limite, deve ser sumarizada ou
	 * truncada.
	 * </ul>
	 * 
	 * @throws LLMException
	 * 
	 * @see LLMProvider#tokenCount(String, String)
	 * @see LLMProvider#sumarizeChat(Chat, String, MapParam)
	 */
	protected void applyRollingWindow()
	            throws LLMException {
		// 1. Verificar se o inputChat já está dentro do limite
		int totalTokens = countTokens(inputChat);
		if (checkCtxSpace(totalTokens)) {
			// nada a fazer
			outputChat = inputChat;
			return;
		}

		totalTokens = countTokens(outputChat);
		if (totalTokens <= maxContextWindow) {
			return;
		}

		// Precisa gerenciar o contexto
		// 2. Manter mensagens do tipo SYSTEM
		Chat managed = new Chat();

		// Deque para empilhar mensagens na ordem inversa
		Deque<Message> messagesStack = new ArrayDeque<>();

		if (inputChat.getMessages() != null) {
			for (Message m : outputChat.getMessages()) {
				if (m.getRole() == MessageRole.SYSTEM) {
					managed.addMessage(m);
				} else {
					messagesStack.push(m);
				}
			}
		}

		// 3. Adicionar mensagens mais recentes até atingir o limite,
		// começando pelo último par de USER-ASSISTANT.
		// Nos seguintes, manter sempre
		if (outputChat.getMessages() != null) {
			int       currentTokens = countTokens(managed);
			Message[] lastPair      = new Message[2]; // [0]=USER, [1]=
			// forçar sumarização dos pares mais antigos, > 2
			//int countPairs = 0;

			while (!messagesStack.isEmpty() && currentTokens < maxContextWindow) {
				Message m = messagesStack.pop();
				if (m.getRole() == MessageRole.ASSISTANT) {
					lastPair[1] = m;
				} else if (m.getRole() == MessageRole.USER) {
					lastPair[0] = m;
				}
				// Se temos um par completo, adicionamos
				if (lastPair[0] != null && lastPair[1] != null) {
					//countPairs++;
					int pairTokens = countTokens(lastPair[0]) + countTokens(lastPair[1]);
					if (checkCtxSpace(pairTokens + currentTokens)) {
						managed.addMessage(lastPair[0]);
						managed.addMessage(lastPair[1]);
						currentTokens += pairTokens;
						// reset
						lastPair[0] = null;
						lastPair[1] = null;
					} else { // sumarizar o par e adicionar
						if (llmService != null) {
							Chat toSummarize = new Chat();
							toSummarize.addMessage(lastPair[0]);
							toSummarize.addMessage(lastPair[1]);

							Chat summary = llmService.sumarizeChat(toSummarize, PROMPT_SUMMARIZE, null);
							// checa se a sumarização coberá no contexto
							int summaryTokens = countTokens(summary);

							if ((maxContextWindow - (summaryTokens + currentTokens)) <= minimumContextWindow) {
								lastPair[1] = summary.popMessage(); // USER
								lastPair[0] = summary.popMessage(); // ASSISTANT

								managed.addMessage(lastPair[0]);
								managed.addMessage(lastPair[1]);
							} // if

						} // if llmService

					}
				}
			}
		}
	}

	/**
	 * Checa se há espaço suficiente no contexto, considerando o valor a ser
	 * incluido.
	 * 
	 * @param valueCheck - valor a ser incluído no contexto
	 * 
	 * @return true se houver espaço suficiente, false caso contrário
	 * 
	 * @see #maxContextWindow
	 * @see #minimumContextWindow
	 */
	private boolean checkCtxSpace(int valueCheck) {
		return (getMaxContextWindow() - valueCheck) >= minimumContextWindow;
	}

	public Chat getManagedChat() { return this.outputChat; }

	private int countTokens(Message msg) {
		if (msg == null) {
			return 0;
		}
		return msg.countTokens(llmService);
	}

	private int countTokens(Chat chat) {
		int total = 0;
		if (chat != null && chat.getMessages() != null) {
			for (Message m : chat.getMessages()) {
				if (m.getContent() != null) {
					int count = m.countTokens(llmService);
					total += count;
				}
			}
		}
		return total;
	}

}
