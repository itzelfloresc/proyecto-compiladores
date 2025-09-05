package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaToDfaConverter
 * -----------------
 * This class provides a static method to convert a Non-deterministic Finite Automaton (NFA)
 * into a Deterministic Finite Automaton (DFA) using the standard subset construction algorithm.
 */
/**
 * Utility class for converting NFAs to DFAs using the subset construction algorithm.
 */
public class NfaToDfaConverter {
	/**
	 * Default constructor for NfaToDfaConverter.
	 */
		public NfaToDfaConverter() {
			// TODO: Implement constructor if needed
		}

	/**
	 * Converts an NFA to a DFA using the subset construction algorithm.
	 * Each DFA state represents a set of NFA states. Final states are marked if any NFA state in the set is final.
	 *
	 * @param nfa The input NFA
	 * @param alphabet The input alphabet (set of characters)
	 * @return The resulting DFA
	 */
	public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
		// TODO: Implement convertNfaToDfa
		/*
		 Pseudocode:
		 1. Create initial DFA state from epsilon-closure of NFA start state
		 2. While there are unmarked DFA states:
			  - For each symbol in alphabet:
				  - Compute move and epsilon-closure for current DFA state
				  - If target set is new, create new DFA state and add to list/queue
				  - Add transition from current to target DFA state
		 3. Mark DFA states as final if any NFA state in their set is final
		 4. Return DFA with start state and all DFA states
		*/
		// 1. Crear estado inicial DFA desde epsilon-closure del estado inicial NFA
		Set<State> initialNfaStates = new HashSet<>();
		initialNfaStates.add(nfa.getStartState());
		Set<State> initialClosure = epsilonClosure(initialNfaStates);
		
		DfaState startDfaState = new DfaState(initialClosure);
		
		// Lista para almacenar todos los estados DFA
		List<DfaState> allDfaStates = new ArrayList<>();
		allDfaStates.add(startDfaState);
		
		// Cola para procesar estados DFA no marcados
		Queue<DfaState> unmarkedStates = new LinkedList<>();
		unmarkedStates.offer(startDfaState);

		// 2. Procesar mientras haya estados no marcados
		while (!unmarkedStates.isEmpty()) {
			DfaState currentDfaState = unmarkedStates.poll();
			
			// Para cada símbolo en el alfabeto
			for (char symbol : alphabet) {
				// Calcular move y epsilon-closure
				Set<State> moveResult = move(currentDfaState.getName(), symbol);
				Set<State> targetNfaStates = epsilonClosure(moveResult);
				
				// Si el conjunto resultante no está vacío
				if (!targetNfaStates.isEmpty()) {
					// Buscar si ya existe un estado DFA para este conjunto
					DfaState targetDfaState = findDfaState(allDfaStates, targetNfaStates);
					
					// Si no existe, crear uno nuevo
					if (targetDfaState == null) {
						targetDfaState = new DfaState(targetNfaStates);
						allDfaStates.add(targetDfaState);
						unmarkedStates.offer(targetDfaState);  // Agregar para procesar
					}
					
					// Agregar transición del estado actual al destino
					currentDfaState.addTransition(symbol, targetDfaState);
				}
			}
		}
		
		// 3. Marcar estados DFA como finales si contienen algún estado NFA final
		for (DfaState dfaState : allDfaStates) {
			boolean isFinal = false;
			for (State nfaState : dfaState.getName()) {
				if (nfaState.isFinal()) {
					isFinal = true;
					break;
				}
			}
			dfaState.setFinal(isFinal);
		}
		
		// 4. Retornar DFA completo
		return new DFA(startDfaState, allDfaStates);
	}

	/**
	 * Computes the epsilon-closure of a set of NFA states.
	 * The epsilon-closure is the set of states reachable by epsilon (null) transitions.
	 *
	 * @param states The set of NFA states.
	 * @return The epsilon-closure of the input states.
	 */
	private static Set<State> epsilonClosure(Set<State> states) {
		// TODO: Implement epsilonClosure
		/*
		Pseudocode:
		1. Initialize closure with input states
		2. Use stack to process states
		3. For each state, add all reachable states via epsilon transitions
		4. Return closure set
		*/
		Set<State> closure = new HashSet<>(states);  // Inicializar con estados de entrada
		Stack<State> stack = new Stack<>();
		
		// Agregar todos los estados iniciales a la pila
		for (State state : states) {
			stack.push(state);
		}
		
		// Procesar mientras haya estados en la pila
		while (!stack.isEmpty()) {
			State current = stack.pop();
			
			// Obtener todas las transiciones epsilon del estado actual
			List<State> epsilonTransitions = current.getEpsilonTransitions();
			
			// Para cada estado alcanzable por epsilon
			for (State nextState : epsilonTransitions) {
				// Si no está ya en el cierre, agregarlo
				if (!closure.contains(nextState)) {
					closure.add(nextState);
					stack.push(nextState);  // Procesar también este estado
				}
			}
		}
		
		return closure;
	}

	/**
	 * Returns the set of states reachable from a set of NFA states by a given symbol.
	 *
	 * @param states The set of NFA states.
	 * @param symbol The input symbol.
	 * @return The set of reachable states.
	 */
	private static Set<State> move(Set<State> states, char symbol) {
		// TODO: Implement move
		/*
		 Pseudocode:
		 1. For each state in input set:
			  - For each transition with given symbol:
				  - Add destination state to result set
		 2. Return result set
		*/
		Set<State> result = new HashSet<>();
    
		// Para cada estado en el conjunto de entrada
		for (State state : states) {
			// Obtener todos los estados alcanzables con el símbolo dado
			List<State> transitions = state.getTransitions(symbol);
			
			// Agregar todos los estados destino al resultado
			for (State nextState : transitions) {
				result.add(nextState);
			}
		}
		
		return result;
	}

	/**
	 * Finds an existing DFA state representing a given set of NFA states.
	 *
	 * @param dfaStates The list of DFA states.
	 * @param targetNfaStates The set of NFA states to search for.
	 * @return The matching DFA state, or null if not found.
	 */
	private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
	   // TODO: Implement findDfaState
	   /*
	    Pseudocode:
	    1. For each DFA state in list:
		    - If its NFA state set equals target set, return DFA state
	    2. If not found, return null
	   */
		for (DfaState dfaState : dfaStates) {
			// Si el conjunto de estados NFA es igual al objetivo
			if (dfaState.getName().equals(targetNfaStates)) {
				return dfaState;
			}
		}
		return null;
	}
}